package com.esunbank.ecommerce.controller;

import com.esunbank.ecommerce.dto.ApiResponse;
import com.esunbank.ecommerce.dto.ProductDTO;
import com.esunbank.ecommerce.dto.UpdateStockRequest;
import com.esunbank.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAvailableProducts() {
        List<ProductDTO> products = productService.getAvailableProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> addProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.addProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Product added successfully"));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        productService.updateStock(productId, request.getAddQuantity());
        return ResponseEntity.ok(ApiResponse.success(null, "Stock updated successfully"));
    }
}
