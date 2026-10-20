package com.gabriel.crudjava.controller;

import com.gabriel.crudjava.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerControllerTest {

    private static final String BASE_URL = "/customers";
    private static final String EXISTING_EMAIL = "joao.silva@email.com";
    private static final long NON_EXISTENT_ID = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    private Long existingId;

    @BeforeEach
    void setUp() {
        existingId = customerRepository.findByEmail(EXISTING_EMAIL).orElseThrow().getId();
    }

    private String body(String name, String email, String phone, String address) {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "phone": "%s",
                  "address": "%s"
                }
                """.formatted(name, email, phone, address);
    }

    // ---------- GET /customers ----------

    @Test
    void findAll_shouldReturn200WithCustomerList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].email", hasItem(EXISTING_EMAIL)));
    }

    // ---------- GET /customers/{id} ----------

    @Test
    void findById_shouldReturn200_whenCustomerExists() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", existingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.email").value(EXISTING_EMAIL))
                .andExpect(jsonPath("$.phone").value("(11) 99999-1111"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void findById_shouldReturn404_whenCustomerDoesNotExist() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cliente não encontrado com o id: " + NON_EXISTENT_ID))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ---------- POST /customers ----------

    @Test
    void create_shouldReturn201WithLocation_whenDataIsValid() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ana Costa", "ana.costa@email.com", "(41) 96666-4444", "Rua XV, 10 - Curitiba/PR")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE_URL + "/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ana Costa"))
                .andExpect(jsonPath("$.email").value("ana.costa@email.com"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void create_shouldReturn400_whenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Outro Joao", EXISTING_EMAIL, "(11) 90000-0000", "Rua A, 1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema."));
    }

    @Test
    void create_shouldReturn422_whenRequiredFieldsAreEmpty() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "", "", "")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors[*].field", hasItems("name", "email", "phone")));
    }

    @Test
    void create_shouldReturn422_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ana Costa", "email-invalido", "(41) 96666-4444", "Rua XV, 10")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')].message", hasItem("Email inválido")));
    }

    // ---------- PUT /customers/{id} ----------

    @Test
    void update_shouldReturn200_whenDataIsValid() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("João Atualizado", EXISTING_EMAIL, "(11) 90000-0000", "Rua Nova, 1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name").value("João Atualizado"))
                .andExpect(jsonPath("$.phone").value("(11) 90000-0000"))
                .andExpect(jsonPath("$.address").value("Rua Nova, 1"));
    }

    @Test
    void update_shouldReturn404_whenCustomerDoesNotExist() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", NON_EXISTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Qualquer", "qualquer@email.com", "(11) 90000-0000", "Rua A, 1")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_shouldReturn400_whenNewEmailBelongsToAnotherCustomer() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("João Silva", "maria.oliveira@email.com", "(11) 99999-1111", "Rua A, 1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema."));
    }

    @Test
    void update_shouldReturn422_whenDataIsInvalid() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", EXISTING_EMAIL, "(11) 99999-1111", "Rua A, 1")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].message", hasItem("Nome é obrigatório")));
    }

    // ---------- DELETE /customers/{id} ----------

    @Test
    void delete_shouldReturn204_whenCustomerExists() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", existingId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/{id}", existingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn404_whenCustomerDoesNotExist() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------- CORS ----------

    @Test
    void cors_shouldReturnHeaders_whenOriginIsAllowed() throws Exception {
        mockMvc.perform(options(BASE_URL)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    void cors_shouldReject_whenOriginIsNotAllowed() throws Exception {
        mockMvc.perform(options(BASE_URL)
                        .header("Origin", "http://site-nao-permitido.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
