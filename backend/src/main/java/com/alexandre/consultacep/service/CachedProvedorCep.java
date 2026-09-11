package com.alexandre.consultacep.service;

import com.alexandre.consultacep.client.ProvedorCep;
import com.alexandre.consultacep.domain.Endereco;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedProvedorCep {

    private final ProvedorCep provedorCep;

    public CachedProvedorCep(ProvedorCep provedorCep) {
        this.provedorCep = provedorCep;
    }

    @Cacheable("enderecosPorCep")
    public Endereco consultar(String cep) {
        return provedorCep.consultar(cep);
    }
}
