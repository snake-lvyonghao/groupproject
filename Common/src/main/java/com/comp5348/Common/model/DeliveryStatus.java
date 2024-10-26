package com.comp5348.Common.model;

public enum DeliveryStatus {
    REQUEST_RECEIVED, //接收到送货请求
    PREPARED, //准备
    SHIPPED,
    DELIVERED,
    LOST,//这个代表丢件了
    CANCELLED,;
}
