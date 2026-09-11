package com.alexandre.consultacep.security;

import com.alexandre.consultacep.client.ProvedorCep;
import com.alexandre.consultacep.domain.Endereco;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @MockitoBean
    private ProvedorCep provedorCep;

    @Test
    void deveRetornar401QuandoApiKeyHabilitadaESemHeader() throws Exception {
        mockMvc.perform(get("/api/v1/ceps/01001000"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("API_KEY_INVALIDA"));
        
        verifyNoInteractions(provedorCep);
    }

    @Test
    void deveRetornar401QuandoApiKeyHabilitadaEHeaderIncorreto() throws Exception {
        mockMvc.perform(get("/api/v1/ceps/01001000")
                        .header("X-API-Key", "chave-errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("API_KEY_INVALIDA"));
        
        verifyNoInteractions(provedorCep);
    }

    @Test
    void deveRetornar200Ou404QuandoApiKeyCorreta() throws Exception {
        Endereco enderecoPadrao = new Endereco(
            "01001-000", "Praça da Sé", "lado ímpar", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", "VIACEP"
        );
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao);

        mockMvc.perform(get("/api/v1/ceps/01001000")
                        .header("X-API-Key", "teste-chave-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"));

        verify(provedorCep).consultar("01001000");
    }

    @Test
    void naoDeveBloquearOptionsMesmoComApiKeyHabilitada() throws Exception {
        mockMvc.perform(options("/api/v1/ceps/01001000")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
}
