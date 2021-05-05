package com.techrocking.order.service;

import com.techrocking.order.entity.Customer;
import com.techrocking.order.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class CustomerService {


    private final CustomerRepository customerRepository;


    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer findCustomer(Long id){
        return customerRepository.findById(id).get();
    }
}
