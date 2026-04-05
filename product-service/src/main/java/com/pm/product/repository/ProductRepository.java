package com.pm.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findBySlug(String slug);
	List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(String name, String description, String brand);
}
