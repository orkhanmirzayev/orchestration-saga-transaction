package com.techrocking.inventory.service;

import com.techrocking.inventory.entity.ProductInventory;
import com.techrocking.inventory.repository.ProductInventoryRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ProductInventoryService {

    private final ProductInventoryRepository productInventoryRepository;


    public ProductInventoryService(ProductInventoryRepository productInventoryRepository) {
        this.productInventoryRepository = productInventoryRepository;
    }

    @Transactional
    public void saveNewInventory(ProductInventory productInventory){
        productInventoryRepository.save(productInventory);
    }
}
