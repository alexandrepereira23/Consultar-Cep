package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.RespostaInvalidaException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ViaCepClient implements ProvedorCep {

    private static final String CEP_COM_MASCARA = "\\d{5}-\\d{3}";

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
                            throw new RespostaInvalidaException("Fornecedor retornou status 400 para a consulta de CEP.");
                        }
                        throw new ServicoIndisponivelException("Fornecedor retornou status " + res.getStatusCode() + ".");
                    })
                    .body(ViaCepResponse.class);

            validarResposta(response);

            if (Boolean.TRUE.equals(response.erro())) {
                throw new CepNaoEncontradoException("Fornecedor indicou CEP inexistente: " + cep + ".");
            }

            return converterParaDominio(response);

        } catch (CepNaoEncontradoException | RespostaInvalidaException | ServicoIndisponivelException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException("Falha de acesso ao fornecedor de CEP: " + e.getMessage());
        } catch (RestClientException e) {
            throw new RespostaInvalidaException("Resposta do fornecedor de CEP não pôde ser processada: " + e.getMessage());
        }
    }

    private void validarResposta(ViaCepResponse viaCep) {
        if (viaCep == null) {
            throw new RespostaInvalidaException("Fornecedor retornou resposta nula.");
        }
        if (Boolean.TRUE.equals(viaCep.erro())) {
            return;
        }
        if (viaCep.cep() == null || !viaCep.cep().matches(CEP_COM_MASCARA)) {
            throw new RespostaInvalidaException("Fornecedor retornou CEP ausente ou inválido.");
        }
        if (viaCep.localidade() == null || viaCep.localidade().isBlank()) {
            throw new RespostaInvalidaException("Fornecedor retornou cidade ausente.");
        }
        if (viaCep.uf() == null || viaCep.uf().isBlank() || viaCep.uf().length() != 2) {
            throw new RespostaInvalidaException("Fornecedor retornou UF ausente ou inválida.");
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
