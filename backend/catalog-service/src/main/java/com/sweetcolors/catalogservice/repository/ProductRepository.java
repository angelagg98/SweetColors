package com.sweetcolors.catalogservice.repository;

import com.sweetcolors.catalogservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(Product.Category category);

    boolean existsByName(String name);
}