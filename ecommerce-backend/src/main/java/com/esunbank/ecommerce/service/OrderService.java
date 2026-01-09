package com.esunbank.ecommerce.service;

import com.esunbank.ecommerce.dto.*;
import com.esunbank.ecommerce.entity.Order;
import com.esunbank.ecommerce.entity.OrderDetail;
import com.esunbank.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private static final AtomicInteger orderCounter = new AtomicInteger(1);

    public OrderDTO createOrder(OrderDTO orderDTO) {
        String orderId = generateOrderId();

        Map<String, Object> result = orderRepository.createOrder(
                orderId,
                orderDTO.getMemberId(),
                orderDTO.getOrderItems());

        int resultCode = (int) result.get("result");
        String message = (String) result.get("message");

        if (resultCode != 1) {
            throw new RuntimeException(message);
        }

        BigDecimal totalPrice = (BigDecimal) result.get("totalPrice");

        List<OrderDetail> details = orderRepository.getOrderDetails(orderId);
        List<OrderItemDTO> items = details.stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(orderId)
                .memberId(orderDTO.getMemberId())
                .totalPrice(totalPrice)
                .payStatus(0)
                .orderStatus(0)
                .orderItems(items)
                .build();
    }

    public OrderDTO getOrderDetails(String orderId) {
        List<OrderDetail> details = orderRepository.getOrderDetails(orderId);

        if (details.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        List<OrderItemDTO> items = details.stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList());

        BigDecimal totalPrice = items.stream()
                .map(OrderItemDTO::getItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderDTO.builder()
                .orderId(orderId)
                .totalPrice(totalPrice)
                .orderItems(items)
                .build();
    }

    public List<OrderDTO> getMemberOrders(String memberId) {
        return orderRepository.getMemberOrders(memberId).stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.getAllOrders().stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    public void payOrder(String orderId) {
        Map<String, Object> result = orderRepository.payOrder(orderId);

        int resultCode = (int) result.get("result");
        String message = (String) result.get("message");

        if (resultCode != 1) {
            throw new RuntimeException(message);
        }
    }

    public void updateOrderStatus(String orderId, Integer status) {
        Map<String, Object> result = orderRepository.updateOrderStatus(orderId, status);

        int resultCode = (int) result.get("result");
        String message = (String) result.get("message");

        if (resultCode != 1) {
            throw new RuntimeException(message);
        }
    }

    private String generateOrderId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int counter = orderCounter.getAndIncrement() % 1000;
        return String.format("Ms%s%03d", timestamp, counter);
    }

    private OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .memberId(order.getMemberId())
                .totalPrice(order.getTotalPrice())
                .payStatus(order.getPayStatus())
                .orderStatus(order.getOrderStatus())
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderDetail detail) {
        return OrderItemDTO.builder()
                .orderItemSn(detail.getOrderItemSn())
                .productId(detail.getProductId())
                .productName(detail.getProductName())
                .quantity(detail.getQuantity())
                .standPrice(detail.getStandPrice())
                .itemPrice(detail.getItemPrice())
                .build();
    }
}
