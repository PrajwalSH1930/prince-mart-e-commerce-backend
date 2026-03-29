package com.pm.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.product.client.AuditClient; // Ensure you've created this Feign client
import com.pm.product.dto.AuditLogRequest; // Ensure you've created this DTO
import com.pm.product.entity.Category;
import com.pm.product.exception.ResourceNotFoundException;
import com.pm.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public CategoryService(CategoryRepository categoryRepository, 
                           AuditClient auditClient, 
                           ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    public Category createCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        
        // Audit the creation of a new category
        // userId is null here assuming this is called by an Admin (you can pass AdminID if available)
        sendAuditLog(null, "CREATE_CATEGORY", null, savedCategory);
        
        return savedCategory;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    // Centralized Helper for External Auditing
    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "PRODUCT-SERVICE", // Category belongs to the Product microservice logic
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Category Service: " + e.getMessage());
        }
    }
}