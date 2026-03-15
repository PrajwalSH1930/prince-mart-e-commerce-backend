package com.pm.product.service;

import com.pm.product.client.InventoryClient;
import com.pm.product.dto.ProductDTO;
import com.pm.product.dto.ProductVariantDTO;
import com.pm.product.entity.Category;
import com.pm.product.entity.Product;
import com.pm.product.entity.ProductVariant;
import com.pm.product.exception.ResourceNotFoundException;
import com.pm.product.repository.CategoryRepository;
import com.pm.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryClient inventoryClient; // Added Feign Client

    public ProductService(ProductRepository productRepository, 
                          CategoryRepository categoryRepository, 
                          InventoryClient inventoryClient) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryClient = inventoryClient;
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
        
        // Auto-generate slug: "Nike Air Max" -> "nike-air-max-123456789"
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
        
        // Save the product and variants first to get their IDs
        Product savedProduct = productRepository.save(product);

        // AUTOMATION: Call Inventory Service for each variant
        // Assuming '1L' is the default Warehouse ID you created earlier
        for (ProductVariant savedVariant : savedProduct.getVariants()) {
            inventoryClient.initializeStock(
                savedProduct.getProductId(),
                savedVariant.getVariantId(),
                1L, 
                savedVariant.getStockQuantity(), // Sending initial stock quantity from DTO
                "INITIAL_PRODUCT_UPLOAD"
            );
        }

        return savedProduct;
    }
    
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}