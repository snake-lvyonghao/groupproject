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

        // 执行 commit 操作
        Future<CommitResponse> commitResponseFuture = bankStub.commit(CommitRequest.newBuilder()
                .setTransactionId(transactionId)
                .build());

        try {
            CommitResponse commitResponse = commitResponseFuture.get();
            if (commitResponse.getSuccess()) {
                // 如果 commit 成功，更新事务状态为 SUCCESS
                Transaction transaction = transactionRepository.findById(Long.valueOf(transactionId)).orElse(null);
                if (transaction != null) {
                    transaction.setStatus(SUCCESS);
                    transactionRepository.save(transaction);
                }
                return true;
            } else {
                // 如果 commit 失败，执行回滚
                rollbackTransaction(context);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            rollbackTransaction(context);
            throw new RuntimeException("Commit phase failed.", e);
        }
    }


    public void rollbackTransaction(BusinessActionContext context) {
        Integer transactionId = (Integer) context.getActionContext("transactionId");

        // 执行 rollback 操作
        Future<RollbackResponse> rollbackResponseFuture = bankStub.rollback(RollbackRequest.newBuilder()
                .setTransactionId(transactionId)
                .build());

        try {
            RollbackResponse rollbackResponse = rollbackResponseFuture.get();
            if (rollbackResponse.getSuccess()) {
                // 更新事务状态为 FAILED
                Transaction transaction = transactionRepository.findById(Long.valueOf(transactionId)).orElse(null);
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
