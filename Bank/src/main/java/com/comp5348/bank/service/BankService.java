package com.comp5348.bank.service;

import com.comp5348.bank.model.Account;
import com.comp5348.bank.model.TransactionRecord;
import com.comp5348.bank.model.TransactionStatus;
import com.comp5348.bank.repository.AccountRepository;
import com.comp5348.bank.repository.TransactionRecordRepository;
import com.comp5348.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

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
        // 获取from账户和to账户
        Account fromAccount = accountRepository.findByAccountOwner(request.getFromAccount());
        Account toAccount = accountRepository.findByAccountOwner(request.getToAccount());

        if (fromAccount == null || toAccount == null) {
            // 如果任一账户不存在，返回失败
            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        // 准备事务记录
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setFromAccount(fromAccount.getAccountOwner());
        transactionRecord.setToAccount(toAccount.getAccountOwner());
        transactionRecord.setAmount(request.getAmount());
        transactionRecord.setStatus(TransactionStatus.PENDING);
        transactionRecord.setId(request.getTransactionId());

        // 检查from账户的余额是否足够
        if (fromAccount.getBalance() >= request.getAmount()) {
            // 将from账户的余额锁定（相当于冻结该金额，等待commit）
            fromAccount.setBalance(fromAccount.getBalance() - request.getAmount());
            transactionRecordRepository.save(transactionRecord);
            accountRepository.save(fromAccount);

            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(true).build());
        } else {
            // 余额不足，设置事务状态为失败
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecordRepository.save(transactionRecord);

            responseObserver.onNext(PrepareResponse.newBuilder().setSuccess(false).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void commit(CommitRequest request, StreamObserver<CommitResponse> responseObserver) {
        TransactionRecord transactionRecord = transactionRecordRepository.findByTransactionId(request.getTransactionId()).orElse(null);
        if (transactionRecord == null || transactionRecord.getStatus() != TransactionStatus.PENDING) {
            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        // 获取from账户和to账户
        Account fromAccount = accountRepository.findByAccountOwner(transactionRecord.getFromAccount());
        Account toAccount = accountRepository.findByAccountOwner(transactionRecord.getToAccount());

        if (fromAccount == null || toAccount == null) {
            transactionRecord.setStatus(TransactionStatus.FAILURE);
            transactionRecordRepository.save(transactionRecord);

            responseObserver.onNext(CommitResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        // 将to账户的余额增加
        toAccount.setBalance(toAccount.getBalance() + transactionRecord.getAmount());
        transactionRecord.setStatus(TransactionStatus.SUCCESS);

        // 保存更新
        transactionRecordRepository.save(transactionRecord);
        accountRepository.save(toAccount);

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

        // 获取from账户和to账户
        Account fromAccount = accountRepository.findByAccountOwner(transactionRecord.getFromAccount());
        Account toAccount = accountRepository.findByAccountOwner(transactionRecord.getToAccount());

        // 如果账户不存在，无法进行回滚
        if (fromAccount == null || toAccount == null) {
            responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }

        // 根据不同的事务状态进行处理
        switch (transactionRecord.getStatus()) {
            case PENDING:
                // 如果事务仍处于PENDING状态，意味着扣款未正式提交，可以直接取消
                fromAccount.setBalance(fromAccount.getBalance() + transactionRecord.getAmount());
                transactionRecord.setStatus(TransactionStatus.CANCELLED);

                // 保存更新
                accountRepository.save(fromAccount);
                transactionRecordRepository.save(transactionRecord);

                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
                break;

            case SUCCESS:
                // 如果事务已成功，需要撤销对两个账户的变动
                fromAccount.setBalance(fromAccount.getBalance() + transactionRecord.getAmount());
                toAccount.setBalance(toAccount.getBalance() - transactionRecord.getAmount());
                transactionRecord.setStatus(TransactionStatus.ROLLED_BACK);

                // 保存更新
                accountRepository.save(fromAccount);
                accountRepository.save(toAccount);
                transactionRecordRepository.save(transactionRecord);

                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
                break;

            case FAILURE:
                // 如果事务已经标记为失败，直接返回成功，因为失败的事务不需要进行任何操作
                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(true).build());
                break;

            default:
                // 其他未知状态，回滚失败
                responseObserver.onNext(RollbackResponse.newBuilder().setSuccess(false).build());
                break;
        }

        responseObserver.onCompleted();
    }
}
