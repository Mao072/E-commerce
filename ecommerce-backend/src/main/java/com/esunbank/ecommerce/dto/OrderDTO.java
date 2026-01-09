package com.esunbank.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private String orderId;

    @NotBlank(message = "Member ID is required")
    private String memberId;

    private BigDecimal totalPrice;
    private Integer payStatus;
    private Integer orderStatus;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDTO> orderItems;
}
