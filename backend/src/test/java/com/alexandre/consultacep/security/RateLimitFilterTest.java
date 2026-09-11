package com.alexandre.consultacep.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private RateLimitProperties properties;
    private ApiKeyProperties apiKeyProperties;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setHabilitado(true);
        properties.setRequisicoes(2);
        properties.setJanela(Duration.ofMinutes(1));
        
        apiKeyProperties = new ApiKeyProperties();
        apiKeyProperties.setHabilitada(false);

        rateLimitFilter = new RateLimitFilter(properties, apiKeyProperties);
    }

    @Test
    void devePermitirRequisicaoQuandoNaoExcederLimite() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("2", response.getHeader("X-RateLimit-Limit"));
        assertEquals("1", response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void deveBloquearRequisicaoQuandoExcederLimite() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request.setRemoteAddr("127.0.0.1");

        // Req 1 - Allowed
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response1, new MockFilterChain());
        assertEquals(200, response1.getStatus());

        // Req 2 - Allowed
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response2, new MockFilterChain());
        assertEquals(200, response2.getStatus());

        // Req 3 - Blocked
        MockHttpServletResponse response3 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response3, new MockFilterChain());
        assertEquals(429, response3.getStatus());
        assertEquals("0", response3.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void devePermitirQuandoDesabilitado() throws Exception {
        properties.setHabilitado(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request.setRemoteAddr("127.0.0.1");

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void deveSepararLimitePorApiKey() throws Exception {
        apiKeyProperties.setHabilitada(true);

        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request1.addHeader("X-API-Key", "key1");
        request1.setRemoteAddr("127.0.0.1");

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request2.addHeader("X-API-Key", "key2");
        request2.setRemoteAddr("127.0.0.1");

        // Consume 2 from key1
        rateLimitFilter.doFilterInternal(request1, new MockHttpServletResponse(), new MockFilterChain());
        rateLimitFilter.doFilterInternal(request1, new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletResponse response1Blocked = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request1, response1Blocked, new MockFilterChain());
        assertEquals(429, response1Blocked.getStatus());

        // key2 should still be allowed
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request2, response2, new MockFilterChain());
        assertEquals(200, response2.getStatus());
        assertEquals("1", response2.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void deveIgnorarApiKeyQuandoDesabilitadaEAgruparPorIp() throws Exception {
        apiKeyProperties.setHabilitada(false);

        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request1.addHeader("X-API-Key", "key1");
        request1.setRemoteAddr("127.0.0.1");

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request2.addHeader("X-API-Key", "key2");
        request2.setRemoteAddr("127.0.0.1");

        MockHttpServletRequest request3 = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request3.addHeader("X-API-Key", "key3");
        request3.setRemoteAddr("127.0.0.1");

        // Request 1 with key1 -> Allowed
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request1, response1, new MockFilterChain());
        assertEquals(200, response1.getStatus());

        // Request 2 with key2 from same IP -> Allowed
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request2, response2, new MockFilterChain());
        assertEquals(200, response2.getStatus());

        // Request 3 with key3 from same IP -> Blocked because limit is 2 per IP
        MockHttpServletResponse response3 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request3, response3, new MockFilterChain());
        assertEquals(429, response3.getStatus());
    }

    @Test
    void deveSepararLimitePorIpQuandoApiKeyDesabilitada() throws Exception {
        apiKeyProperties.setHabilitada(false);

        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request1.setRemoteAddr("127.0.0.1");

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/ceps/01001000");
        request2.setRemoteAddr("127.0.0.2");

        // Consume 2 from IP 1
        rateLimitFilter.doFilterInternal(request1, new MockHttpServletResponse(), new MockFilterChain());
        rateLimitFilter.doFilterInternal(request1, new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletResponse response1Blocked = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request1, response1Blocked, new MockFilterChain());
        assertEquals(429, response1Blocked.getStatus());

        // IP 2 should still be allowed
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request2, response2, new MockFilterChain());
        assertEquals(200, response2.getStatus());
    }

    @Test
    void deveIgnorarOptions() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/ceps/01001000");

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void deveIgnorarRotasForaDeApiV1() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }
    }
}
