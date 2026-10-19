package com.gabriel.crudjava.controller;

import com.gabriel.crudjava.domain.Customer;
import com.gabriel.crudjava.dtos.request.CustomerRequestDTO;
import com.gabriel.crudjava.dtos.response.CustomerResponseDTO;
import com.gabriel.crudjava.services.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> findAll() {
        List<Customer> customers = customerService.findAll();
        List<CustomerResponseDTO> customersDTO = customers.stream()
                .map(CustomerResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(customersDTO);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CustomerResponseDTO> findById(@PathVariable Long id) {
        Customer customer = customerService.findById(id);

        return ResponseEntity.ok().body(new CustomerResponseDTO(customer));
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
        Customer customer = customerService.create(customerRequestDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(customer.getId()).toUri();

        return ResponseEntity.created(uri).body(new CustomerResponseDTO(customer));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
        Customer customer = customerService.update(id, customerRequestDTO);

        return ResponseEntity.ok().body(new CustomerResponseDTO(customer));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
