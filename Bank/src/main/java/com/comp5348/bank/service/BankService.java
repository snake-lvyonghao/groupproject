package com.comp5348.bank.service;

import com.comp5348.bank.model.Account;
import com.comp5348.bank.model.TransactionRecord;
import com.comp5348.bank.model.TransactionStatus;
import com.comp5348.bank.repository.AccountRepository;
import com.comp5348.bank.repository.TransactionRecordRepository;
import com.comp5348.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


import static com.comp5348.grpc.PrepareRequest.Action.ADD_BALANCE;
import static com.comp5348.grpc.PrepareRequest.Action.REDUCE_BALANCE;

@GrpcService
public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public BankService(AccountRepository accountRepository, TransactionRecordRepository transactionRecordRepository) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Override
    public void prepare(PrepareRequest request, StreamObserver<PrepareResponse> responseObserver) {
        TransactionRecord transactionRecord = new TransactionRecord();

        //根据请求查找账户
        Account account = accountRepository.findByAccountOwner(request.getCusomerName());
        if (account == null) {
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecord.setAccountId(account.getAccountId());
            transactionRecordRepository.save(transactionRecord);

            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        transactionRecord.setAccountId(account.getAccountId());
        transactionRecord.setAmount(request.getMoney());
        if (request.getAction() == REDUCE_BALANCE) {
            transactionRecord.setAmount(-request.getMoney());
        } else if (request.getAction() == ADD_BALANCE) {
            transactionRecord.setAmount(request.getMoney());
        }
        transactionRecord.setStatus(TransactionStatus.PENDING);

        if (request.getAction() == REDUCE_BALANCE && account.getBalance() >= request.getMoney()) {
            transactionRecord.setStatus(TransactionStatus.PENDING);
        } else if (request.getAction() == ADD_BALANCE) {
            transactionRecord.setStatus(TransactionStatus.PENDING);
        } else {
            transactionRecord.setStatus(TransactionStatus.FAILURE);
        }

        transactionRecordRepository.save(transactionRecord);
        if (transactionRecord.getStatus() == TransactionStatus.PENDING) {
            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
            return;
        }

        responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(false).build());
        responseObserver.onCompleted();
    }

    @Override
    public void commit(CommitRequest request, StreamObserver<CommitResponse> responseObserver) {
        TransactionRecord transactionRecord = transactionRecordRepository.findByTransactionId(request.getTransactionId()).orElse(null);
        if (transactionRecord == null) {
            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        Account acc = accountRepository.findById(transactionRecord.getAccountId()).orElse(null);
        if (acc == null) {
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecordRepository.save(transactionRecord);

            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        acc.setBalance(acc.getBalance() + transactionRecord.getAmount());
        transactionRecord.setStatus(TransactionStatus.SUCCESS);
        transactionRecordRepository.save(transactionRecord);
        accountRepository.save(acc);

        responseObserver.onNext(CommitResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void rollback(RollbackRequest request, StreamObserver<RollbackResponse> responseObserver) {
        TransactionRecord transactionRecord = transactionRecordRepository.findByTransactionId(request.getTransactionId()).orElse(null);
        if (transactionRecord == null) {
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        Account acc = accountRepository.findById(transactionRecord.getAccountId()).orElse(null);
        if (acc == null) {
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        if (transactionRecord.getStatus() == TransactionStatus.FAILURE) {
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
            return;
        } else if (transactionRecord.getStatus() == TransactionStatus.PENDING) {
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecordRepository.save(transactionRecord);
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
            return;
        } else if (transactionRecord.getStatus() == TransactionStatus.SUCCESS) {
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecordRepository.save(transactionRecord);
            acc.setBalance(acc.getBalance() - transactionRecord.getAmount());
            accountRepository.save(acc);
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
            return;
        }
        responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
        responseObserver.onCompleted();
    }
}
