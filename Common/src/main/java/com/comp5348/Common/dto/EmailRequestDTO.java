package com.comp5348.Common.dto;

import com.comp5348.Common.model.DeliveryStatus;
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
public class EmailRequestDTO {
    @JsonProperty("customer_name")
    private String customerName;
    @JsonProperty("customer_email")
    private String customerEmail;
    private DeliveryStatus status;
}
