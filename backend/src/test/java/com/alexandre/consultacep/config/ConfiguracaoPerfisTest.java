package com.alexandre.consultacep.config;

import com.alexandre.consultacep.security.ApiKeyProperties;
import com.alexandre.consultacep.security.RateLimitProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoPerfisTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("local")
    class PerfilLocalTest {

        @Autowired
        private ApiKeyProperties apiKeyProperties;

        @Autowired
        private RateLimitProperties rateLimitProperties;

        @Value("${aplicacao.cors.origens-permitidas}")
        private List<String> origensPermitidas;

        @Test
        void deveCarregarConfiguracoesDoPerfilLocal() {
            assertFalse(apiKeyProperties.isHabilitada());
            assertTrue(rateLimitProperties.isHabilitado());
            assertEquals(120, rateLimitProperties.getRequisicoes());
            assertEquals(Duration.ofMinutes(1), rateLimitProperties.getJanela());
            assertEquals(List.of("http://localhost:4200"), origensPermitidas);
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("prod")
    @TestPropertySource(properties = {
            "API_KEY_VALOR=chave-secreta-prod",
            "CORS_ORIGENS_PERMITIDAS=https://consultar-cep.vercel.app"
    })
    class PerfilProdTest {

        @Autowired
        private ApiKeyProperties apiKeyProperties;

        @Autowired
        private RateLimitProperties rateLimitProperties;

        @Value("${aplicacao.cors.origens-permitidas}")
        private List<String> origensPermitidas;

        @Test
        void deveCarregarConfiguracoesDoPerfilProd() {
            assertTrue(apiKeyProperties.isHabilitada());
            assertEquals("chave-secreta-prod", apiKeyProperties.getValor());
            assertTrue(rateLimitProperties.isHabilitado());
            assertEquals(60, rateLimitProperties.getRequisicoes());
            assertEquals(Duration.ofMinutes(1), rateLimitProperties.getJanela());
            assertEquals(List.of("https://consultar-cep.vercel.app"), origensPermitidas);
        }
    }
}
