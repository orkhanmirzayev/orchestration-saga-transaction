package com.techrocking.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techrocking.inventory.controller.OrderRequest;
import com.techrocking.inventory.entity.ProductInventory;
import com.techrocking.inventory.kafka.source.ItemFetchedEventSource;
import com.techrocking.inventory.kafka.source.ItemOutOfStockEventSource;
import com.techrocking.inventory.repository.ProductInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
//import com.techrocking.inventory.kafka.source.ItemOutOfStockEventSource;

@Service
public class InventoryService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(InventoryService.class);
	
	private final ItemFetchedEventSource itemFetchedEventSource;

	public InventoryService(ItemFetchedEventSource itemFetchedEventSource, ProductInventoryRepository productInventoryRepository, ItemOutOfStockEventSource itemOutOfStockEventSource) {
		this.itemFetchedEventSource = itemFetchedEventSource;
		this.productInventoryRepository = productInventoryRepository;
		this.itemOutOfStockEventSource = itemOutOfStockEventSource;
	}
//
	private final ItemOutOfStockEventSource itemOutOfStockEventSource;

	private final ProductInventoryRepository productInventoryRepository;

	public void fetchItem(OrderRequest orderRequest) throws JsonProcessingException {
		
		/*inventory service will call order service to find out item id for order id*/
		ProductInventory productFromInventory = getProductFromInventory(orderRequest.getProductCode());

		Boolean isInStock = productFromInventory.getQuantity().intValue() >= orderRequest.getQuantity();
		if(isInStock) {
			productFromInventory.setQuantity(productFromInventory.getQuantity().intValue() - orderRequest.getQuantity());
			productInventoryRepository.save(productFromInventory);
			itemFetchedEventSource.publishItemFetchedEvent(orderRequest);
			logger.info("item is fetched successfully");
		}else {
			itemOutOfStockEventSource.publishItemOutOfStockEvent(orderRequest);
			logger.info("item is out of stock");
		}
	}
	
	public void compensateItem(Long orderId) {
		/*return given item id to inventory*/
//		Long itemId = getItemFromOrderService(orderId);
		
		/*this will compensate order*/
//		itemOutOfStockEventSource.publishItemOutOfStockEvent(orderId, itemId);
	}
	
	private ProductInventory getProductFromInventory(String productCode) {
		return productInventoryRepository.findByCode(productCode);
	}
	
	private boolean isItemExistsInItemDatabase(Long itemId) {
		return true;
	}

}
