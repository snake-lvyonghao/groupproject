package com.comp5348.store.service;

import com.comp5348.grpc.BankServiceGrpc;
import com.comp5348.grpc.CommitRequest;
import com.comp5348.grpc.CommitResponse;
import com.comp5348.grpc.PrepareRequest;
import com.comp5348.grpc.PrepareResponse;
import com.comp5348.grpc.RollbackRequest;
import com.comp5348.grpc.RollbackResponse;
import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.model.Transaction;
import com.comp5348.store.repository.TransactionRepository;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.comp5348.store.model.Transaction.TransactionStatus.*;


@Service
@Slf4j(topic = "com.comp5348.store")
@LocalTCC
public class BankService {

    @GrpcClient("bankService")
    private BankServiceGrpc.BankServiceFutureStub bankStub;

    private final TransactionRepository transactionRepository;

    public BankService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @TwoPhaseBusinessAction(name = "prepareBankTransaction", commitMethod = "commitTransaction", rollbackMethod = "rollbackTransaction")
    public boolean prepareTransaction(BusinessActionContext context, OrderDTO order, boolean isRefund) {
        //isRefund True refund False payment
        String fromAccount = isRefund ? "Store" : order.getCustomer().getName();
        String toAccount = isRefund ? order.getCustomer().getName() : "Store";

        // Create a transaction record and save it as OPEN
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(order.getTotalPrice());
        transaction.setStatus(OPEN);
        transaction = transactionRepository.save(transaction);

        // Save the transaction ID in context for use during the commit and rollback phases
        context.addActionContext("transactionId", transaction.getId());
        log.info(transaction.getFromAccount() +  " " +transaction.getToAccount() + " " + transaction.getAmount());
        // Perform bank debit operations
        Future<PrepareResponse> bankResponseFuture = bankStub.prepare(PrepareRequest.newBuilder()
                .setFromAccount(fromAccount)
                .setToAccount(toAccount)
                .setAmount(order.getTotalPrice())
                .setTransactionId(transaction.getId())
                .build());

        try {
            PrepareResponse bankResponse = bankResponseFuture.get();
            if (bankResponse.getSuccess()) {
                // Return true if prepare succeeds
                return true;
            } else {
                // If prepare fails, the transaction status is changed to FAILED
                transaction.setStatus(FAILED);
                transactionRepository.save(transaction);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            // If an exception occurs, update the transaction status to FAILED
            transaction.setStatus(FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Prepare phase failed.", e);
        }
    }

    public boolean commitTransaction(BusinessActionContext context) {
        Integer transactionId = (Integer) context.getActionContext("transactionId");

        // Get transaction record
        Transaction transaction = transactionRepository.findById(Long.valueOf(transactionId)).orElse(null);
        if (transaction == null || transaction.getStatus() == SUCCESS) {
            log.info("Transaction {} is already committed or does not exist.", transactionId);
            return true; // If the transaction has been successfully committed or does not exist, return success directly
        }

        log.info("Attempting to commit transaction with ID: {}", transactionId);

        // grpc commit
        Future<CommitResponse> commitResponseFuture = bankStub.commit(CommitRequest.newBuilder()
                .setTransactionId(transactionId)
                .build());

        try {
            CommitResponse commitResponse = commitResponseFuture.get();
            if (commitResponse.getSuccess()) {
                // If commit succeeds, update the transaction status to SUCCESS
                transaction.setStatus(SUCCESS);
                transactionRepository.save(transaction);
                log.info("Commit successful for transactionId: {}", transactionId);
                return true;
            } else {
                log.warn("Commit failed for transactionId: {}", transactionId);
                return false; // Inform Seata that the submission failed, and Seata will decide whether to retry or roll back
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred during commit for transactionId: {}. Error: {}", transactionId, e.getMessage(), e);
            return false; // Return false and let Seata handle retry or rollback logic
        }
    }



    public boolean rollbackTransaction(BusinessActionContext context) {
        Integer transactionId = (Integer) context.getActionContext("transactionId");

        // 获取事务记录
        Transaction transaction = transactionRepository.findById(Long.valueOf(transactionId)).orElse(null);
        if (transaction == null || transaction.getStatus() == FAILED) {
            log.info("Transaction {} is already rolled back or does not exist.", transactionId);
            return true; // If the transaction has been rolled back or does not exist, return success directly to avoid repeated rollbacks
        }

        log.info("Attempting to rollback transaction with ID: {}", transactionId);

        // 执行 rollback 操作
        Future<RollbackResponse> rollbackResponseFuture = bankStub.rollback(RollbackRequest.newBuilder()
                .setTransactionId(transactionId)
                .build());

        try {
            RollbackResponse rollbackResponse = rollbackResponseFuture.get();
            if (rollbackResponse.getSuccess()) {
                // 如果 rollback 成功，更新事务状态为 FAILED
                transaction.setStatus(FAILED);
                transactionRepository.save(transaction);
                log.info("Rollback successful for transactionId: {}", transactionId);
                    return true; // Successfully rolled back
            } else {
                log.error("Rollback failed for transactionId: {}", transactionId);
                return false; // Inform Seata that the rollback failed
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred during rollback for transactionId: {}. Error: {}", transactionId, e.getMessage(), e);
            return false; // Return false and let Seata decide what to do
        }
    }

}
