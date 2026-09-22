package com.sweetcolors.catalogservice.service;

import com.sweetcolors.catalogservice.dto.ProductRequest;
import com.sweetcolors.catalogservice.dto.ProductResponse;
import com.sweetcolors.catalogservice.dto.StockResponse;
import com.sweetcolors.catalogservice.entity.Product;
import com.sweetcolors.catalogservice.exception.ProductNotFoundException;
import com.sweetcolors.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> findAll(Product.Category category) {
        if (category == null) {
            return productRepository.findAll().stream()
                    .map(ProductResponse::fromEntity)
                    .toList();
        }
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return ProductResponse.fromEntity(getProduct(id));
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre");
        }

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        return ProductResponse.fromEntity(productRepository.save(product));
    }

    public void delete(Long id) {
        productRepository.delete(getProduct(id));
    }

    public StockResponse checkStock(Long id) {
        Product product = getProduct(id);
        return StockResponse.fromEntity(product.getId(), product.getStock());
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}