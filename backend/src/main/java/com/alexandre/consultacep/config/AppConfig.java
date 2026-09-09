package com.alexandre.consultacep.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${integracoes.viacep.timeout-conexao}")
    private Duration timeoutConexao;

    @Value("${integracoes.viacep.timeout-leitura}")
    private Duration timeoutLeitura;

    @Bean
    public RestClient viaCepRestClient(@Value("${integracoes.viacep.url-base}") String urlBase) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeoutConexao.toMillis());
        factory.setReadTimeout((int) timeoutLeitura.toMillis());
        return RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(factory)
                .build();
    }
}
