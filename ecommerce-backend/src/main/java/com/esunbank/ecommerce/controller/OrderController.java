package com.esunbank.ecommerce.controller;

import com.esunbank.ecommerce.dto.*;
import com.esunbank.ecommerce.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            HttpServletRequest request) {

        // Get memberId from JWT token (set by JwtAuthenticationFilter)
        String memberId = (String) request.getAttribute("memberId");
        if (memberId != null) {
            orderDTO.setMemberId(memberId);
        }

        OrderDTO created = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Order created successfully"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetails(@PathVariable String orderId) {
        OrderDTO order = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(HttpServletRequest request) {
        String memberId = (String) request.getAttribute("memberId");
        List<OrderDTO> orders = orderService.getMemberOrders(memberId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(null, "Order status updated successfully"));
    }
}
