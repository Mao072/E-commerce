package com.esunbank.ecommerce.service;

import com.esunbank.ecommerce.dto.ProductDTO;
import com.esunbank.ecommerce.entity.Product;
import com.esunbank.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getAvailableProducts() {
        return productRepository.findAvailable().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO addProduct(ProductDTO productDTO) {
        Map<String, Object> result = productRepository.addProduct(productDTO);

        int resultCode = (int) result.get("result");
        String message = (String) result.get("message");

        if (resultCode != 1) {
            throw new RuntimeException(message);
        }

        return productDTO;
    }

    public void updateStock(String productId, Integer addQuantity) {
        Map<String, Object> result = productRepository.updateStock(productId, addQuantity);

        int resultCode = (int) result.get("result");
        String message = (String) result.get("message");

        if (resultCode != 1) {
            throw new RuntimeException(message);
        }
    }

    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}
