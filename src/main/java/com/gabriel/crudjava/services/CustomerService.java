package com.gabriel.crudjava.services;

import com.gabriel.crudjava.domain.Customer;
import com.gabriel.crudjava.dtos.request.CustomerRequestDTO;
import com.gabriel.crudjava.repositories.CustomerRepository;
import com.gabriel.crudjava.services.exceptions.DataIntegrityViolationException;
import com.gabriel.crudjava.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.orElseThrow(() -> new ObjectNotFoundException("Cliente não encontrado com o id: " + id));
    }

    public Customer create(CustomerRequestDTO customerRequestDTO) {
        if (customerRepository.existsByEmail(customerRequestDTO.getEmail())) {
            throw new DataIntegrityViolationException("Email já cadastrado no sistema.");
        }

        Customer customer = new Customer();
        customer.setName(customerRequestDTO.getName());
        customer.setEmail(customerRequestDTO.getEmail());
        customer.setPhone(customerRequestDTO.getPhone());
        customer.setAddress(customerRequestDTO.getAddress());

        return customerRepository.save(customer);
    }

    public Customer update(Long id, CustomerRequestDTO customerRequestDTO) {
        Customer customer = findById(id);

        if (!customer.getEmail().equals(customerRequestDTO.getEmail())
                && customerRepository.existsByEmail(customerRequestDTO.getEmail())) {
            throw new DataIntegrityViolationException("Email já cadastrado no sistema.");
        }

        customer.setName(customerRequestDTO.getName());
        customer.setEmail(customerRequestDTO.getEmail());
        customer.setPhone(customerRequestDTO.getPhone());
        customer.setAddress(customerRequestDTO.getAddress());
        customer.setUpdatedAt(LocalDateTime.now());

        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        findById(id);
        customerRepository.deleteById(id);
    }
}
