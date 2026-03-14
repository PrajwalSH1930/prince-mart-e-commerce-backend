package com.pm.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.product.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

}
