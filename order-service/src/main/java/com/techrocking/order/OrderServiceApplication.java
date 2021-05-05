package com.techrocking.order;

import com.techrocking.order.entity.Customer;
import com.techrocking.order.entity.Product;
import com.techrocking.order.repository.CustomerRepository;
import com.techrocking.order.repository.OrderItemRepository;
import com.techrocking.order.repository.OrderRepository;
import com.techrocking.order.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication implements CommandLineRunner {
	
	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private OrderItemRepository orderItemRepository;
	@Autowired
	private ProductRepository productRepository;

	@Override
	public void run(String... args) throws Exception {
		Customer customer = new Customer();
		customer.setName("Orkhan");
		customer.setPhoneNo("123456763");
		customerRepository.save(customer);

		Product product = new Product();
		product.setDescription("Omega 3 for children");
		product.setName("Fish oil");
		productRepository.save(product);
//		System.out.println(createdProduct);
//		Product persistenceProduct = productRepository.findById(createdProduct.getId()).get();


	}
}
