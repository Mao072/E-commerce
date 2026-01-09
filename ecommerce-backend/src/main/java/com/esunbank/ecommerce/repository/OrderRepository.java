package com.esunbank.ecommerce.repository;

import com.esunbank.ecommerce.dto.OrderItemDTO;
import com.esunbank.ecommerce.entity.Order;
import com.esunbank.ecommerce.entity.OrderDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private final RowMapper<Order> orderRowMapper = (ResultSet rs, int rowNum) -> {
        Order order = new Order();
        order.setOrderId(rs.getString("order_id"));
        order.setMemberId(rs.getString("member_id"));
        try {
            order.setUsername(rs.getString("username"));
        } catch (Exception e) {
            // username may not be present in all queries
        }
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        order.setPayStatus(rs.getInt("pay_status"));
        order.setOrderStatus(rs.getInt("order_status"));
        return order;
    };

    private final RowMapper<OrderDetail> orderDetailRowMapper = (ResultSet rs, int rowNum) -> {
        OrderDetail detail = new OrderDetail();
        detail.setOrderItemSn(rs.getLong("order_item_sn"));
        detail.setOrderId(rs.getString("order_id"));
        detail.setProductId(rs.getString("product_id"));
        detail.setProductName(rs.getString("product_name"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setStandPrice(rs.getBigDecimal("stand_price"));
        detail.setItemPrice(rs.getBigDecimal("item_price"));
        return detail;
    };

    public Map<String, Object> createOrder(String orderId, String memberId, List<OrderItemDTO> items) {
        Map<String, Object> result = new HashMap<>();

        try {
            String itemsJson = objectMapper.writeValueAsString(items);

            jdbcTemplate.update(
                    "CALL sp_create_order(?, ?, ?, @p_result, @p_message, @p_total_price)",
                    orderId,
                    memberId,
                    itemsJson);

            Map<String, Object> outParams = jdbcTemplate.queryForMap(
                    "SELECT @p_result as result, @p_message as message, @p_total_price as total_price");

            result.put("result", ((Number) outParams.get("result")).intValue());
            result.put("message", outParams.get("message"));
            result.put("totalPrice",
                    outParams.get("total_price") != null ? new BigDecimal(outParams.get("total_price").toString())
                            : BigDecimal.ZERO);

        } catch (JsonProcessingException e) {
            log.error("Error serializing order items", e);
            result.put("result", -1);
            result.put("message", "Error processing order items");
        }

        return result;
    }

    public List<OrderDetail> getOrderDetails(String orderId) {
        return jdbcTemplate.query("CALL sp_get_order_details(?)", orderDetailRowMapper, orderId);
    }

    public List<Order> getMemberOrders(String memberId) {
        return jdbcTemplate.query("CALL sp_get_member_orders(?)", orderRowMapper, memberId);
    }

    public List<Order> getAllOrders() {
        return jdbcTemplate.query("CALL sp_get_all_orders()", orderRowMapper);
    }

    public Map<String, Object> payOrder(String orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = jdbcTemplate.update("UPDATE `order` SET pay_status = 1 WHERE order_id = ?", orderId);
            if (rows > 0) {
                result.put("result", 1);
                result.put("message", "Payment successful");
            } else {
                result.put("result", 0);
                result.put("message", "Order not found");
            }
        } catch (Exception e) {
            log.error("Payment failed", e);
            result.put("result", -1);
            result.put("message", "Database error");
        }
        return result;
    }

    public Map<String, Object> updateOrderStatus(String orderId, Integer status) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = jdbcTemplate.update("UPDATE `order` SET order_status = ? WHERE order_id = ?", status, orderId);
            if (rows > 0) {
                result.put("result", 1);
                result.put("message", "Order status updated successfully");
            } else {
                result.put("result", 0);
                result.put("message", "Order not found");
            }
        } catch (Exception e) {
            log.error("Update status failed", e);
            result.put("result", -1);
            result.put("message", "Database error");
        }
        return result;
    }
}
