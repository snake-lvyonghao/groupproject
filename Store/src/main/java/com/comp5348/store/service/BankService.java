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
import com.comp5348.store.model.TransactionStatus;
import com.comp5348.store.repository.TransactionRepository;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.comp5348.store.model.TransactionStatus.*;

@Service
public class BankService {

    @GrpcClient("bankService")
    private BankServiceGrpc.BankServiceFutureStub bankStub;

    private final TransactionRepository transactionRepository;

    public BankService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }


    public boolean processTransaction(OrderDTO order, boolean isRefund) {
        // 设置转账的方向
        String fromAccount = isRefund ? "Store" : order.getCustomer().getName();
        String toAccount = isRefund ? order.getCustomer().getName() : "Store";

        // 创建事务记录并保存为OPEN状态
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(order.getTotalPrice());
        transaction.setStatus(OPEN);
        transaction = transactionRepository.save(transaction);

        // 执行银行扣款操作
        Future<PrepareResponse> bankResponseFuture = bankStub.prepare(PrepareRequest.newBuilder()
                .setFromAccount(fromAccount)
                .setToAccount(toAccount)
                .setAmount(order.getTotalPrice())
                .setTransactionId(transaction.getId())
                .build());

        PrepareResponse bankResponse;
        try {
            bankResponse = bankResponseFuture.get();
            if (bankResponse.getSuccess()) {
                // 如果prepare成功，继续执行commit
                return commitTransaction(transaction.getId());
            } else {
                // 如果prepare失败，执行回滚
                rollbackTransaction(transaction.getId());
                transaction.setStatus(FAILED);
                transactionRepository.save(transaction);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            // 如果出现异常，执行回滚
            rollbackTransaction(transaction.getId());
            transaction.setStatus(FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Prepare phase failed.", e);
        }
    }


    private boolean commitTransaction(Long transactionId) {
        // 执行commit操作
        Future<CommitResponse> commitResponseFuture = bankStub.commit(CommitRequest.newBuilder()
                .setTransactionId(transactionId)
                .build());

        try {
            CommitResponse commitResponse = commitResponseFuture.get();
            if (commitResponse.getSuccess()) {
                // 如果commit成功，更新事务状态为SUCCESS
                Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
                if (transaction != null) {
                    transaction.setStatus(SUCCESS);
                    transactionRepository.save(transaction);
                }
                return true;
            } else {
                // 如果commit失败，执行回滚
                rollbackTransaction(transactionId);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            rollbackTransaction(transactionId);
            throw new RuntimeException("Commit phase failed.", e);
        }
    }

    private void rollbackTransaction(Long transactionId) {
        // 执行rollback操作
        Future<RollbackResponse> rollbackResponseFuture = bankStub.rollback(RollbackRequest.newBuilder()
                .setTransactionId(transactionId)
                .build());

        try {
            RollbackResponse rollbackResponse = rollbackResponseFuture.get();
            if (rollbackResponse.getSuccess()) {
                // 更新事务状态为FAILURE
                Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
                if (transaction != null) {
                    transaction.setStatus(FAILED);
                    transactionRepository.save(transaction);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Rollback phase failed.", e);
        }
    }
}
