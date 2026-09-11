package com.alexandre.consultacep.security;

import com.alexandre.consultacep.exception.ErroResposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties rateLimitProperties;
    private final ApiKeyProperties apiKeyProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static final String API_KEY_HEADER = "X-API-Key";

    public RateLimitFilter(RateLimitProperties rateLimitProperties, ApiKeyProperties apiKeyProperties) {
        this.rateLimitProperties = rateLimitProperties;
        this.apiKeyProperties = apiKeyProperties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitProperties.isHabilitado()) {
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

        String clienteId = resolverClienteId(request);
        Bucket bucket = buckets.computeIfAbsent(clienteId, k -> criarNovoBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimitProperties.getRequisicoes()));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimitProperties.getRequisicoes()));
            response.addHeader("X-RateLimit-Remaining", "0");
            response.addHeader("Retry-After", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            enviarRespostaErro(response, request.getRequestURI());
        }
    }

    private String resolverClienteId(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKeyProperties.isHabilitada() && apiKey != null && !apiKey.isBlank()) {
            return "apikey:" + apiKey;
        }
        String ip = request.getRemoteAddr();
        if (ip == null) {
            ip = "unknown";
        }
        return "ip:" + ip;
    }

    private Bucket criarNovoBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimitProperties.getRequisicoes())
                .refillGreedy(rateLimitProperties.getRequisicoes(), rateLimitProperties.getJanela())
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void enviarRespostaErro(HttpServletResponse response, String path) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErroResposta erro = new ErroResposta(
                429,
                "LIMITE_REQUISICOES_EXCEDIDO",
                "Limite de requisições excedido. Tente novamente mais tarde.",
                path,
                Instant.now()
        );

        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}
