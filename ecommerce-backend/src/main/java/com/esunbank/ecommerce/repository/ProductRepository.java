package com.esunbank.ecommerce.repository;

import com.esunbank.ecommerce.dto.ProductDTO;
import com.esunbank.ecommerce.entity.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Product> productRowMapper = (ResultSet rs, int rowNum) -> {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setQuantity(rs.getInt("quantity"));
        try {
            product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        } catch (Exception e) {
            // Some queries may not return these fields
        }
        return product;
    };

    public List<Product> findAll() {
        return jdbcTemplate.query("CALL sp_get_all_products()", productRowMapper);
    }

    public List<Product> findAvailable() {
        return jdbcTemplate.query("CALL sp_get_available_products()", productRowMapper);
    }

    public Map<String, Object> addProduct(ProductDTO productDTO) {
        Map<String, Object> result = new HashMap<>();

        jdbcTemplate.update(
                "CALL sp_add_product(?, ?, ?, ?, @p_result, @p_message)",
                productDTO.getProductId(),
                productDTO.getProductName(),
                productDTO.getPrice(),
                productDTO.getQuantity());

        Map<String, Object> outParams = jdbcTemplate.queryForMap(
                "SELECT @p_result as result, @p_message as message");

        result.put("result", ((Number) outParams.get("result")).intValue());
        result.put("message", outParams.get("message"));

        return result;
    }
}
