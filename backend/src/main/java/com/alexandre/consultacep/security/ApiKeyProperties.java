package com.alexandre.consultacep.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "aplicacao.api-key")
public class ApiKeyProperties {

    private boolean habilitada = false;
    private String valor;

    public boolean isHabilitada() {
        return habilitada;
    }

    public void setHabilitada(boolean habilitada) {
        this.habilitada = habilitada;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @PostConstruct
    public void validarConfiguracao() {
        if (habilitada && (valor == null || valor.trim().isEmpty())) {
            throw new IllegalStateException("API key habilitada, mas o valor da chave não foi configurado. Verifique aplicacao.api-key.valor!");
        }
    }
}
