package com.comp5348.bank.service;

import com.comp5348.bank.model.Account;
import com.comp5348.bank.model.TransactionRecord;
import com.comp5348.bank.model.TransactionStatus;
import com.comp5348.bank.repository.AccountRepository;
import com.comp5348.bank.repository.TransactionRecordRepository;
import com.comp5348.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@GrpcService
@Service
@Slf4j(topic = "com.comp5348.bank")
public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public BankService(AccountRepository accountRepository, TransactionRecordRepository transactionRecordRepository) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Override
    @Transactional
    public void prepare(PrepareRequest request, StreamObserver<PrepareResponse> responseObserver) {
        // Added a 5-second delay
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error("Error during delay", e);
            responseObserver.onError(e);
            return;
        }
        Account fromAccount = accountRepository.findByAccountOwner(request.getFromAccount());
        Account toAccount = accountRepository.findByAccountOwner(request.getToAccount());

        if (fromAccount == null || toAccount == null) {
            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setFromAccount(fromAccount.getAccountOwner());
        transactionRecord.setToAccount(toAccount.getAccountOwner());
        transactionRecord.setAmount(request.getAmount());
        transactionRecord.setStatus(TransactionStatus.PENDING);
        transactionRecord.setId(request.getTransactionId());

        if (fromAccount.getBalance() >= request.getAmount()) {
            //  blocked funds
            fromAccount.setFrozenAmount(fromAccount.getFrozenAmount() + request.getAmount());
            transactionRecordRepository.save(transactionRecord);
            accountRepository.save(fromAccount);

            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();

            // Set a timeout task and check whether it needs to be rolled back after 5 seconds
            scheduler.schedule(() -> {
                TransactionRecord pendingTransaction = transactionRecordRepository.findById(request.getTransactionId()).orElse(null);
                if (pendingTransaction.getStatus() == TransactionStatus.PENDING) {
                    log.info("Rolling back transaction due to timeout: {}", request.getTransactionId());
                    rollback(RollbackRequest.newBuilder().setTransactionId(request.getTransactionId()).build(), new StreamObserver<RollbackResponse>() {
                        @Override
                        public void onNext(RollbackResponse value) {
                            log.info("Rollback response received for transactionId: {}", request.getTransactionId());
                        }
                        @Override
                        public void onError(Throwable t) {
                            log.error("Error during rollback for transactionId: {}", request.getTransactionId(), t);
                        }
                        @Override
                        public void onCompleted() {
                            log.info("Rollback completed for transactionId: {}", request.getTransactionId());
                        }
                    });
                }
            }, 5, TimeUnit.SECONDS);

        } else {
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecordRepository.save(transactionRecord);
            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
        }
    }
    @Override
    @Transactional
    public void commit(CommitRequest request, StreamObserver<CommitResponse> responseObserver) {
        log.info("Received commit request for transactionId: {}", request.getTransactionId());

        // Search Transaction record
        TransactionRecord transactionRecord = transactionRecordRepository.findById(request.getTransactionId()).orElse(null);
        if (transactionRecord == null) {
            log.warn("Transaction record not found for transactionId: {}", request.getTransactionId());
            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        if (transactionRecord.getStatus() != TransactionStatus.PENDING) {
            log.warn("Transaction is not in PENDING state for transactionId: {}. Current status: {}", request.getTransactionId(), transactionRecord.getStatus());
            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        // Get Account information
        Account fromAccount = accountRepository.findByAccountOwner(transactionRecord.getFromAccount());
        Account toAccount = accountRepository.findByAccountOwner(transactionRecord.getToAccount());

        if (fromAccount == null || toAccount == null) {
            log.error("Account not found. FromAccount: {}, ToAccount: {}", transactionRecord.getFromAccount(), transactionRecord.getToAccount());
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecordRepository.save(transactionRecord);
            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        log.info("Processing commit for transactionId: {}. FromAccount: {}, ToAccount: {}, Amount: {}",
                request.getTransactionId(), fromAccount.getAccountOwner(), toAccount.getAccountOwner(), transactionRecord.getAmount());

        // Subtract the frozen amount and increase the receiving account balance
        try {
            double frozenAmount = fromAccount.getFrozenAmount();
            double transactionAmount = transactionRecord.getAmount();
            if (frozenAmount < transactionAmount) {
                log.error("Insufficient frozen amount for commit. FrozenAmount: {}, TransactionAmount: {}", frozenAmount, transactionAmount);
                responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            fromAccount.setFrozenAmount(fromAccount.getFrozenAmount() - transactionAmount);
            fromAccount.setBalance(fromAccount.getBalance() - transactionAmount);
            toAccount.setBalance(toAccount.getBalance() + transactionAmount);

            // Update the transaction status to success
            transactionRecord.setStatus(TransactionStatus.SUCCESS);

            // Keep records of accounts and transactions
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            transactionRecordRepository.save(transactionRecord);

            log.info("Commit successful for transactionId: {}. Updated FromAccount balance: {},Update FromAccount frozenAmount: {} ToAccount balance: {}",
                    request.getTransactionId(), fromAccount.getBalance(),fromAccount.getFrozenAmount(), toAccount.getBalance());
            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(true).build());

        } catch (Exception e) {
            log.error("Exception occurred while committing transactionId: {}. Error: {}", request.getTransactionId(), e.getMessage(), e);
            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
        }

        responseObserver.onCompleted();
    }


    @Override
    @Transactional
    public void rollback(RollbackRequest request, StreamObserver<RollbackResponse> responseObserver) {
        TransactionRecord transactionRecord = transactionRecordRepository.findById(request.getTransactionId()).orElse(null);
        if (transactionRecord == null) {
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        Account fromAccount = accountRepository.findByAccountOwner(transactionRecord.getFromAccount());
        Account toAccount = accountRepository.findByAccountOwner(transactionRecord.getToAccount());

        if (fromAccount == null) {
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        switch (transactionRecord.getStatus()) {
            case PENDING:
                // unfreeze
                fromAccount.setFrozenAmount(fromAccount.getFrozenAmount() - transactionRecord.getAmount());
                transactionRecord.setStatus(TransactionStatus.CANCELLED);
                accountRepository.save(fromAccount);
                transactionRecordRepository.save(transactionRecord);
                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
                break;

            case SUCCESS:
                // Roll back fund transfers
                fromAccount.setBalance(fromAccount.getBalance() + transactionRecord.getAmount());
                toAccount.setBalance(toAccount.getBalance() - transactionRecord.getAmount());
                transactionRecord.setStatus(TransactionStatus.ROLLED_BACK);
                accountRepository.save(fromAccount);
                accountRepository.save(toAccount);
                transactionRecordRepository.save(transactionRecord);
                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
                break;

            case FAILURE:
                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
                break;

            default:
                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
                break;
        }

        responseObserver.onCompleted();
    }

}
