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
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.comp5348.store.model.TransactionStatus.*;


@Service
@Slf4j
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
        String fromAccount = isRefund ? "Store" : order.getCustomer().getName();
        String toAccount = isRefund ? order.getCustomer().getName() : "Store";

        // 创建事务记录并保存为 OPEN 状态
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(order.getTotalPrice());
        transaction.setStatus(OPEN);
        transaction = transactionRepository.save(transaction);

        // 把事务 ID 保存到上下文中以便于 commit 和 rollback 阶段使用
        context.addActionContext("transactionId", transaction.getId());
        log.info(transaction.getFromAccount() +  " " +transaction.getToAccount() + " " + transaction.getAmount());
        // 执行银行扣款操作
        Future<PrepareResponse> bankResponseFuture = bankStub.prepare(PrepareRequest.newBuilder()
                .setFromAccount(fromAccount)
                .setToAccount(toAccount)
                .setAmount(order.getTotalPrice())
                .setTransactionId(transaction.getId())
                .build());

        try {
            PrepareResponse bankResponse = bankResponseFuture.get();
            if (bankResponse.getSuccess()) {
                // 如果 prepare 成功，返回 true
                return true;
            } else {
                // 如果 prepare 失败，更新事务状态为 FAILED
                transaction.setStatus(FAILED);
                transactionRepository.save(transaction);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            // 如果出现异常，更新事务状态为 FAILED
            transaction.setStatus(FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Prepare phase failed.", e);
        }
    }

    public boolean commitTransaction(BusinessActionContext context) {
        Integer transactionId = (Integer) context.getActionContext("transactionId");

        // 获取事务记录
        Transaction transaction = transactionRepository.findById(Long.valueOf(transactionId)).orElse(null);
        if (transaction == null || transaction.getStatus() == SUCCESS) {
            log.info("Transaction {} is already committed or does not exist.", transactionId);
            return true; // 如果事务已经成功提交或者不存在，直接返回成功
        }

        log.info("Attempting to commit transaction with ID: {}", transactionId);

        // 执行 commit 操作
        Future<CommitResponse> commitResponseFuture = bankStub.commit(CommitRequest.newBuilder()
                .setTransactionId(transactionId)
                .build());

        try {
            CommitResponse commitResponse = commitResponseFuture.get();
            if (commitResponse.getSuccess()) {
                // 如果 commit 成功，更新事务状态为 SUCCESS
                transaction.setStatus(SUCCESS);
                transactionRepository.save(transaction);
                log.info("Commit successful for transactionId: {}", transactionId);
                return true;
            } else {
                log.warn("Commit failed for transactionId: {}", transactionId);
                return false; // 告知 Seata 提交失败，Seata 会决定是否重试或回滚
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred during commit for transactionId: {}. Error: {}", transactionId, e.getMessage(), e);
            return false; // 返回 false，让 Seata 处理重试或回滚逻辑
        }
    }



    public boolean rollbackTransaction(BusinessActionContext context) {
        Integer transactionId = (Integer) context.getActionContext("transactionId");

        // 获取事务记录
        Transaction transaction = transactionRepository.findById(Long.valueOf(transactionId)).orElse(null);
        if (transaction == null || transaction.getStatus() == FAILED) {
            log.info("Transaction {} is already rolled back or does not exist.", transactionId);
            return true; // 如果事务已经回滚或者不存在，直接返回成功，避免重复回滚
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
                return true; // 回滚成功
            } else {
                log.error("Rollback failed for transactionId: {}", transactionId);
                return false; // 告知 Seata 回滚失败
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred during rollback for transactionId: {}. Error: {}", transactionId, e.getMessage(), e);
            return false; // 返回 false，让 Seata 决定如何处理
        }
    }

}
