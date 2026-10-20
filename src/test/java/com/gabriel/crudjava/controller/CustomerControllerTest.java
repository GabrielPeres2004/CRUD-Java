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
    private static final String EMAIL_EXISTENTE = "joao.silva@email.com";
    private static final long ID_INEXISTENTE = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    private Long idExistente;

    @BeforeEach
    void setUp() {
        idExistente = customerRepository.findByEmail(EMAIL_EXISTENTE).orElseThrow().getId();
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
    void findAllDeveRetornar200ComListaDeClientes() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].email", hasItem(EMAIL_EXISTENTE)));
    }

    // ---------- GET /customers/{id} ----------

    @Test
    void findByIdDeveRetornar200QuandoClienteExiste() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", idExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.email").value(EMAIL_EXISTENTE))
                .andExpect(jsonPath("$.phone").value("(11) 99999-1111"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void findByIdDeveRetornar404QuandoClienteNaoExiste() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", ID_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cliente não encontrado com o id: " + ID_INEXISTENTE))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ---------- POST /customers ----------

    @Test
    void createDeveRetornar201ComLocationQuandoDadosSaoValidos() throws Exception {
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
    void createDeveRetornar400QuandoEmailJaCadastrado() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Outro Joao", EMAIL_EXISTENTE, "(11) 90000-0000", "Rua A, 1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema."));
    }

    @Test
    void createDeveRetornar422QuandoCamposObrigatoriosEstaoVazios() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "", "", "")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors[*].field", hasItems("name", "email", "phone")));
    }

    @Test
    void createDeveRetornar422QuandoEmailInvalido() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ana Costa", "email-invalido", "(41) 96666-4444", "Rua XV, 10")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')].message", hasItem("Email inválido")));
    }

    // ---------- PUT /customers/{id} ----------

    @Test
    void updateDeveRetornar200QuandoDadosSaoValidos() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("João Atualizado", EMAIL_EXISTENTE, "(11) 90000-0000", "Rua Nova, 1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idExistente))
                .andExpect(jsonPath("$.name").value("João Atualizado"))
                .andExpect(jsonPath("$.phone").value("(11) 90000-0000"))
                .andExpect(jsonPath("$.address").value("Rua Nova, 1"));
    }

    @Test
    void updateDeveRetornar404QuandoClienteNaoExiste() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", ID_INEXISTENTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Qualquer", "qualquer@email.com", "(11) 90000-0000", "Rua A, 1")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateDeveRetornar400QuandoNovoEmailPertenceAOutroCliente() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("João Silva", "maria.oliveira@email.com", "(11) 99999-1111", "Rua A, 1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema."));
    }

    @Test
    void updateDeveRetornar422QuandoDadosSaoInvalidos() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", EMAIL_EXISTENTE, "(11) 99999-1111", "Rua A, 1")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].message", hasItem("Nome é obrigatório")));
    }

    // ---------- DELETE /customers/{id} ----------

    @Test
    void deleteDeveRetornar204QuandoClienteExiste() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", idExistente))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/{id}", idExistente))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeveRetornar404QuandoClienteNaoExiste() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", ID_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------- CORS ----------

    @Test
    void deveRetornarHeadersCorsParaOrigemPermitida() throws Exception {
        mockMvc.perform(options(BASE_URL)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    void deveRejeitarRequisicaoCorsDeOrigemNaoPermitida() throws Exception {
        mockMvc.perform(options(BASE_URL)
                        .header("Origin", "http://site-nao-permitido.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
