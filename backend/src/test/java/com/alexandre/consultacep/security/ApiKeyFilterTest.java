package com.alexandre.consultacep.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "aplicacao.api-key.habilitada=true",
        "aplicacao.api-key.valor=teste-chave-123"
})
class ApiKeyFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornar401QuandoApiKeyHabilitadaESemHeader() throws Exception {
        mockMvc.perform(get("/api/v1/ceps/01001000"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("API_KEY_INVALIDA"));
    }

    @Test
    void deveRetornar401QuandoApiKeyHabilitadaEHeaderIncorreto() throws Exception {
        mockMvc.perform(get("/api/v1/ceps/01001000")
                        .header("X-API-Key", "chave-errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("API_KEY_INVALIDA"));
    }

    @Test
    void deveRetornar200Ou404QuandoApiKeyCorreta() throws Exception {
        // Assume que o endpoint base funciona ou retorna NotFound se o CEP não existir
        // O importante é não retornar 401.
        mockMvc.perform(get("/api/v1/ceps/01001000")
                        .header("X-API-Key", "teste-chave-123"))
                .andExpect(status().isOk());
    }

    @Test
    void naoDeveBloquearOptionsMesmoComApiKeyHabilitada() throws Exception {
        mockMvc.perform(options("/api/v1/ceps/01001000")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
}
