package com.pm.product.controller;

import com.pm.product.dto.ProductDTO;
import com.pm.product.entity.Product;
import com.pm.product.entity.ProductVariant;
import com.pm.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome!! This is the Product Service for Prince Mart by Prince Inc.";
    }

    @PostMapping("/add")
    public ResponseEntity<Product> addProduct(@RequestBody ProductDTO productDto) {
        return ResponseEntity.ok(productService.createProduct(productDto));
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getProductDetails(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }
    
    @GetMapping("/variants/{variantId}")
    public ResponseEntity<ProductVariant> getVariantById(@PathVariable Long variantId) {
        return ResponseEntity.ok(productService.getVariantById(variantId));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam("q") String query) {
        List<Product> results = productService.searchProducts(query);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/variants/all")
    public ResponseEntity<List<ProductVariant>> getAllVariants() {
		return ResponseEntity.ok(productService.getAllVariants());
	}
}