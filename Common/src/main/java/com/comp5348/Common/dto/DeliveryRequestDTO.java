package com.comp5348.Common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryRequestDTO {
    @JsonProperty("order_id")
    private Long orderId;

    private List<WarehouseInfo> warehouseInfos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WarehouseInfo {
        private String warehouseName;
        private String warehouseAddress;
        private String goodsName;
        private int quantity;
    }
}