package com.alexandre.consultacep.security;

import com.alexandre.consultacep.exception.ErroResposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    
    private final ApiKeyProperties apiKeyProperties;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!apiKeyProperties.isHabilitada()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        if (!path.startsWith("/api/v1/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKeyRequest = request.getHeader(API_KEY_HEADER);
        if (apiKeyRequest == null || !apiKeyRequest.equals(apiKeyProperties.getValor())) {
            enviarRespostaErro(response, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void enviarRespostaErro(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErroResposta erro = new ErroResposta(
                HttpServletResponse.SC_UNAUTHORIZED,
                "API_KEY_INVALIDA",
                "API key ausente ou inválida.",
                path,
                Instant.now()
        );

        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}
