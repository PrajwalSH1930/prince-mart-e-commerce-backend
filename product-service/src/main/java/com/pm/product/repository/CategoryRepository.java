package com.pm.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.product.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
