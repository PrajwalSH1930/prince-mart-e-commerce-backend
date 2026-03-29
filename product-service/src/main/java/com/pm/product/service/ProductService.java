package com.pm.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.product.client.AuditClient; // New client
import com.pm.product.client.InventoryClient;
import com.pm.product.dto.AuditLogRequest; // New DTO
import com.pm.product.dto.ProductDTO;
import com.pm.product.dto.ProductVariantDTO;
import com.pm.product.entity.Category;
import com.pm.product.entity.Product;
import com.pm.product.entity.ProductVariant;
import com.pm.product.exception.ResourceNotFoundException;
import com.pm.product.repository.CategoryRepository;
import com.pm.product.repository.ProductRepository;
import com.pm.product.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryClient inventoryClient;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, 
                          CategoryRepository categoryRepository, 
                          ProductVariantRepository productVariantRepository,
                          InventoryClient inventoryClient,
                          AuditClient auditClient,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.inventoryClient = inventoryClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Product createProduct(ProductDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand());
        product.setMainImageUrl(dto.getMainImageUrl());
        product.setCategory(category);
        
        product.setSlug(dto.getName().toLowerCase().replaceAll(" ", "-") + "-" + System.currentTimeMillis());

        List<ProductVariant> variants = new ArrayList<>();
        for (ProductVariantDTO vDto : dto.getVariants()) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSize(vDto.getSize());
            variant.setColor(vDto.getColor());
            variant.setPrice(vDto.getPrice());
            variant.setCurrency(vDto.getCurrency() != null ? vDto.getCurrency() : "INR");
            variant.setStockQuantity(vDto.getStockQuantity());
            variant.setSku(vDto.getSku());
            variants.add(variant);
        }

        product.setVariants(variants);
        
        Product savedProduct = productRepository.save(product);

        // Sync with Inventory
        for (ProductVariant savedVariant : savedProduct.getVariants()) {
            try {
                inventoryClient.initializeStock(
                    savedProduct.getProductId(),
                    savedVariant.getVariantId(),
                    1L, // Default warehouse ID
                    savedVariant.getStockQuantity(),
                    "INITIAL_PRODUCT_UPLOAD"
                );
            } catch (Exception e) {
                System.err.println("Non-critical: Inventory sync failed for variant " + savedVariant.getVariantId());
            }
        }

        // Audit the product creation
        // Note: dataBefore is null because this is a new entry
        sendAuditLog(null, "CREATE_PRODUCT", null, savedProduct);

        return savedProduct;
    }
    
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }
    
    public ProductVariant getVariantById(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found with ID: " + variantId));
    }

    // Centralized Helper for External Auditing
    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "PRODUCT-SERVICE", 
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Product Service: " + e.getMessage());
        }
    }
}