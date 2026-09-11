package com.alexandre.consultacep.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "aplicacao.rate-limit")
public class RateLimitProperties {

    private boolean habilitado = true;
    private long requisicoes = 60;
    private Duration janela = Duration.ofMinutes(1);

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public long getRequisicoes() {
        return requisicoes;
    }

    public void setRequisicoes(long requisicoes) {
        this.requisicoes = requisicoes;
    }

    public Duration getJanela() {
        return janela;
    }

    public void setJanela(Duration janela) {
        this.janela = janela;
    }
}
