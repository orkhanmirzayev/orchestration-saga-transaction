package com.techrocking.inventory;

import com.techrocking.inventory.entity.ProductInventory;
import com.techrocking.inventory.service.ProductInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryServiceApplication implements CommandLineRunner {
	
	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Autowired
	private ProductInventoryService productInventoryService;

	@Override
	public void run(String... args) throws Exception {
		ProductInventory inventory = new ProductInventory();
		inventory.setCode("73KBN");
		inventory.setName("Fish oil");
		inventory.setQuantity(5);
		productInventoryService.saveNewInventory(inventory);

	}
}
