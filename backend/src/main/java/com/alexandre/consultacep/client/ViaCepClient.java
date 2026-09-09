package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.RespostaInvalidaException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class ViaCepClient implements ProvedorCep {

    private final RestClient restClient;

    public ViaCepClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Endereco consultar(String cep) {
        try {
            ViaCepResponse response = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        if (res.getStatusCode() == HttpStatus.BAD_REQUEST) {
                            throw new RespostaInvalidaException("Formato de CEP invalido enviado ao ViaCEP.");
                        }
                        throw new ServicoIndisponivelException("O ViaCEP retornou status " + res.getStatusCode());
                    })
                    .body(ViaCepResponse.class);

            if (response == null) {
                throw new RespostaInvalidaException("Resposta nula do ViaCEP");
            }

            if (Boolean.TRUE.equals(response.erro())) {
                throw new CepNaoEncontradoException("O CEP " + cep + " nao foi encontrado no ViaCEP.");
            }

            return converterParaDominio(response);

        } catch (RestClientException e) {
            throw new ServicoIndisponivelException("Falha ao comunicar com o ViaCEP: " + e.getMessage());
        }
    }

    private Endereco converterParaDominio(ViaCepResponse viaCep) {
        return new Endereco(
                viaCep.cep(),
                viaCep.logradouro() != null ? viaCep.logradouro() : "",
                viaCep.complemento() != null ? viaCep.complemento() : "",
                viaCep.unidade() != null ? viaCep.unidade() : "",
                viaCep.bairro() != null ? viaCep.bairro() : "",
                viaCep.localidade() != null ? viaCep.localidade() : "",
                viaCep.uf() != null ? viaCep.uf() : "",
                viaCep.estado() != null ? viaCep.estado() : "",
                viaCep.regiao() != null ? viaCep.regiao() : "",
                viaCep.ibge() != null ? viaCep.ibge() : "",
                viaCep.ddd() != null ? viaCep.ddd() : "",
                viaCep.siafi() != null ? viaCep.siafi() : "",
                viaCep.gia() != null ? viaCep.gia() : "",
                "VIACEP"
        );
    }
}

