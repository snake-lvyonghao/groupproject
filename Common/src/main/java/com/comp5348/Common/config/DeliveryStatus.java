package com.comp5348.Common.config;

import lombok.Getter;

public enum DeliveryStatus {
    FETCHED(1, "goods fetched"),
    SHIPPING(2, "goods shipping"),
    DELIVERED(3, "goods delivered"),
    LOST(4, "goods lost");

    @Getter
    private final int code;

    @Getter
    private final String msg;
    DeliveryStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgFromCode(int code) {
        for (DeliveryStatus status : DeliveryStatus.values()) {
            if (status.getCode() == code) {
                return status.getMsg();
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

}
