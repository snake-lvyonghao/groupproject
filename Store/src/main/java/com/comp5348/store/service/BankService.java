package com.comp5348.store.service;

import com.comp5348.grpc.BankServiceGrpc;
import com.comp5348.grpc.PrepareRequest;
import com.comp5348.grpc.PrepareResponse;
import com.comp5348.store.dto.OrderDTO;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.comp5348.grpc.PrepareRequest.Action.REDUCE_BALANCE;

public class BankService {
    @GrpcClient("bankService")
    private BankServiceGrpc.BankServiceFutureStub bankStub;
    public boolean preparePayment(OrderDTO order) {
        // 执行银行扣款操作
        Future<PrepareResponse> bankResponseFuture = bankStub.prepare(PrepareRequest.newBuilder()
                .setCusomerName(order.getCustomer().getName())
                .setMoney(order.getTotalPrice())
                .setAction(REDUCE_BALANCE)
                .setTransactionId(order.getId())
                .build());

        PrepareResponse bankResponse;
        try {
            bankResponse = bankResponseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return bankResponse.getSuccess();
    }


}
