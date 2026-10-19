package com.gabriel.crudjava.dtos.response;

import com.gabriel.crudjava.domain.Customer;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    public CustomerResponseDTO(Customer customer) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();
        this.address = customer.getAddress();
        this.createdAt = customer.getCreatedAt();
        this.updatedAt = customer.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
