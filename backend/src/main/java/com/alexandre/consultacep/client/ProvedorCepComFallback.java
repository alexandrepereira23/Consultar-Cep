package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ProvedorCepComFallback implements ProvedorCep {

    private static final Logger log = LoggerFactory.getLogger(ProvedorCepComFallback.class);

    private final ViaCepClient viaCepClient;
    private final BrasilApiCepClient brasilApiCepClient;

    public ProvedorCepComFallback(ViaCepClient viaCepClient, BrasilApiCepClient brasilApiCepClient) {
        this.viaCepClient = viaCepClient;
        this.brasilApiCepClient = brasilApiCepClient;
    }

    @Override
    public Endereco consultar(String cep) {
        try {
            log.info("Tentando consultar CEP {} via ViaCEP", cep);
            return viaCepClient.consultar(cep);
        } catch (CepNaoEncontradoException e) {
            log.warn("CEP {} não encontrado no ViaCEP", cep);
            throw e;
        } catch (Exception e) {
            log.error("Falha ao consultar ViaCEP para o CEP {}, tentando fallback para BrasilAPI. Erro: {}", cep, e.getMessage());
            return brasilApiCepClient.consultar(cep);
        }
    }
}
