package com.techrocking.inventory.repository;

import com.techrocking.inventory.entity.ProductInventory;
import org.springframework.data.repository.CrudRepository;

public interface ProductInventoryRepository extends CrudRepository<ProductInventory,Long> {

    ProductInventory findByCode(String productCode);
}
