package com.gabriel.crudjava.services;

import com.gabriel.crudjava.domain.Customer;
import com.gabriel.crudjava.dtos.request.CustomerRequestDTO;
import com.gabriel.crudjava.repositories.CustomerRepository;
import com.gabriel.crudjava.services.exceptions.DataIntegrityViolationException;
import com.gabriel.crudjava.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final Long ID = 1L;
    private static final String NAME = "João Silva";
    private static final String EMAIL = "joao.silva@email.com";
    private static final String PHONE = "(11) 99999-1111";
    private static final String ADDRESS = "Rua das Flores, 123 - São Paulo/SP";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer(NAME, EMAIL, PHONE, ADDRESS);
        customer.setId(ID);

        requestDTO = new CustomerRequestDTO(NAME, EMAIL, PHONE, ADDRESS);
    }

    @Test
    void findAllDeveRetornarListaDeClientes() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.findAll();

        assertEquals(1, result.size());
        assertEquals(customer, result.get(0));
        verify(customerRepository).findAll();
    }

    @Test
    void findAllDeveRetornarListaVaziaQuandoNaoHaClientes() {
        when(customerRepository.findAll()).thenReturn(List.of());

        List<Customer> result = customerService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdDeveRetornarClienteQuandoExiste() {
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));

        Customer result = customerService.findById(ID);

        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(NAME, result.getName());
        assertEquals(EMAIL, result.getEmail());
    }

    @Test
    void findByIdDeveLancarObjectNotFoundExceptionQuandoNaoExiste() {
        when(customerRepository.findById(ID)).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> customerService.findById(ID));

        assertEquals("Cliente não encontrado com o id: " + ID, ex.getMessage());
    }

    @Test
    void createDeveSalvarClienteQuandoEmailNaoExiste() {
        when(customerRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.create(requestDTO);

        assertNotNull(result);
        assertEquals(ID, result.getId());

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer saved = captor.getValue();
        assertEquals(NAME, saved.getName());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(PHONE, saved.getPhone());
        assertEquals(ADDRESS, saved.getAddress());
    }

    @Test
    void createDeveLancarDataIntegrityViolationExceptionQuandoEmailJaExiste() {
        when(customerRepository.existsByEmail(EMAIL)).thenReturn(true);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> customerService.create(requestDTO));

        assertEquals("Email já cadastrado no sistema.", ex.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateDeveAtualizarClienteQuandoDadosSaoValidos() {
        CustomerRequestDTO novosDados = new CustomerRequestDTO(
                "João Atualizado", "joao.novo@email.com", "(11) 90000-0000", "Rua Nova, 1");

        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("joao.novo@email.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.update(ID, novosDados);

        assertEquals("João Atualizado", result.getName());
        assertEquals("joao.novo@email.com", result.getEmail());
        assertEquals("(11) 90000-0000", result.getPhone());
        assertEquals("Rua Nova, 1", result.getAddress());
        assertNotNull(result.getUpdatedAt());
        verify(customerRepository).save(customer);
    }

    @Test
    void updateNaoDeveVerificarEmailQuandoEmailNaoMudou() {
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.update(ID, requestDTO);

        assertEquals(EMAIL, result.getEmail());
        verify(customerRepository, never()).existsByEmail(anyString());
        verify(customerRepository).save(customer);
    }

    @Test
    void updateDeveLancarDataIntegrityViolationExceptionQuandoNovoEmailJaExiste() {
        CustomerRequestDTO novosDados = new CustomerRequestDTO(
                NAME, "maria.oliveira@email.com", PHONE, ADDRESS);

        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("maria.oliveira@email.com")).thenReturn(true);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> customerService.update(ID, novosDados));

        assertEquals("Email já cadastrado no sistema.", ex.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateDeveLancarObjectNotFoundExceptionQuandoClienteNaoExiste() {
        when(customerRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> customerService.update(ID, requestDTO));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteDeveRemoverClienteQuandoExiste() {
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));

        customerService.delete(ID);

        verify(customerRepository).deleteById(ID);
    }

    @Test
    void deleteDeveLancarObjectNotFoundExceptionQuandoClienteNaoExiste() {
        when(customerRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> customerService.delete(ID));

        verify(customerRepository, never()).deleteById(anyLong());
    }
}
