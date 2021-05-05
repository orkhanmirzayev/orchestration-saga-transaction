package com.techrocking.order.service;

import com.techrocking.order.entity.Product;
import com.techrocking.order.repository.ProductRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void createNew(Product product){
        productRepository.save(product);
    }
}
