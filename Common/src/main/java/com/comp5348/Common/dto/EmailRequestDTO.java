package com.comp5348.Common.dto;

import com.comp5348.Common.model.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailRequestDTO {
    //emailDTO中仅包含客户的ID和邮件内容。
    @JsonProperty("customer_id")
    private Long CustomerId;

    @JsonProperty("content")
    private String Content;
}
