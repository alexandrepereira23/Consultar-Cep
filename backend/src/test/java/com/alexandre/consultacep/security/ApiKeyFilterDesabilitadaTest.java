package com.alexandre.consultacep.security;

import com.alexandre.consultacep.client.ProvedorCep;
import com.alexandre.consultacep.domain.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "aplicacao.api-key.habilitada=false"
})
class ApiKeyFilterDesabilitadaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private ProvedorCep provedorCep;

    private final Endereco enderecoPadrao = new Endereco(
            "01001-000", "Praça da Sé", "lado ímpar", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", "VIACEP"
    );

    @BeforeEach
    void limparCache() {
        var cache = cacheManager.getCache("enderecosPorCep");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void devePermitirAcessoAosEndpointsDeIntegracaoSemApiKeyQuandoDesabilitada() throws Exception {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao);

        mockMvc.perform(get("/api/v1/ceps/01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"));

        mockMvc.perform(get("/api/v1/ceps/01001000/detalhes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"));

        mockMvc.perform(get("/api/v1/ceps/01001000/campos?campos=cep,cidade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"));
    }

    @Test
    void devePermitirAcessoAosEndpointsDoPainelSemApiKeyQuandoDesabilitada() throws Exception {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao);

        mockMvc.perform(get("/api/v1/painel/ceps/01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"));

        mockMvc.perform(get("/api/v1/painel/ceps/01001000/detalhes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"));
    }
}
