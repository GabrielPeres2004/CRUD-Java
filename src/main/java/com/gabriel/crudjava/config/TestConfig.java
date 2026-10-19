package com.gabriel.crudjava.config;

import com.gabriel.crudjava.domain.Customer;
import com.gabriel.crudjava.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@Configuration
@Profile("test")
public class TestConfig implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        Customer c1 = new Customer("João Silva", "joao.silva@email.com", "(11) 99999-1111", "Rua das Flores, 123 - São Paulo/SP");
        Customer c2 = new Customer("Maria Oliveira", "maria.oliveira@email.com", "(21) 98888-2222", "Av. Atlântica, 456 - Rio de Janeiro/RJ");
        Customer c3 = new Customer("Carlos Santos", "carlos.santos@email.com", "(31) 97777-3333", "Rua Ouro Preto, 789 - Belo Horizonte/MG");

        customerRepository.saveAll(Arrays.asList(c1, c2, c3));
    }
}
