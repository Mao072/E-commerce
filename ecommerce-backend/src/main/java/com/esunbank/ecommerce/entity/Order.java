package com.esunbank.ecommerce.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String memberId;
    private String username;
    private BigDecimal totalPrice;
    private Integer payStatus;
    private Integer orderStatus;
}
