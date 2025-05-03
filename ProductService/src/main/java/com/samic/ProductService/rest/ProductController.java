package com.samic.ProductService.rest;

import com.samic.ProductService.rest.dto.CreateProductRequest;
import com.samic.ProductService.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService createProduct;

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody CreateProductRequest product) {
        var productId = createProduct.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }
}
