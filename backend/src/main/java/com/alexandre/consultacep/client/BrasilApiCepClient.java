package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.RespostaInvalidaException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BrasilApiCepClient implements ProvedorCep {

    private static final String CEP_COM_MASCARA = "\\d{5}-\\d{3}";
    private static final String CEP_SEM_MASCARA = "\\d{8}";

    private final RestClient restClient;

    public BrasilApiCepClient(@Qualifier("brasilApiRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Endereco consultar(String cep) {
        try {
            BrasilApiResponse response = restClient.get()
                    .uri("/{cep}", cep)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        if (res.getStatusCode() == HttpStatus.NOT_FOUND) {
                            throw new CepNaoEncontradoException("Fornecedor indicou CEP inexistente: " + cep + ".");
                        }
                        if (res.getStatusCode() == HttpStatus.BAD_REQUEST) {
                            throw new RespostaInvalidaException("Fornecedor retornou status 400 para a consulta de CEP.");
                        }
                        throw new ServicoIndisponivelException("Fornecedor retornou status " + res.getStatusCode() + ".");
                    })
                    .body(BrasilApiResponse.class);

            validarResposta(response);

            return converterParaDominio(response);

        } catch (CepNaoEncontradoException | RespostaInvalidaException | ServicoIndisponivelException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException("Falha de acesso ao fornecedor de CEP: " + e.getMessage());
        } catch (RestClientException e) {
            throw new RespostaInvalidaException("Resposta do fornecedor de CEP não pôde ser processada: " + e.getMessage());
        }
    }

    private void validarResposta(BrasilApiResponse response) {
        if (response == null) {
            throw new RespostaInvalidaException("Fornecedor retornou resposta nula.");
        }
        if (response.cep() == null || (!response.cep().matches(CEP_COM_MASCARA) && !response.cep().matches(CEP_SEM_MASCARA))) {
            throw new RespostaInvalidaException("Fornecedor retornou CEP ausente ou inválido.");
        }
        if (response.city() == null || response.city().isBlank()) {
            throw new RespostaInvalidaException("Fornecedor retornou cidade ausente.");
        }
        if (response.state() == null || response.state().isBlank() || response.state().length() != 2) {
            throw new RespostaInvalidaException("Fornecedor retornou UF ausente ou inválida.");
        }
    }

    private Endereco converterParaDominio(BrasilApiResponse response) {
        String cepFormatado = response.cep();
        if (cepFormatado != null && cepFormatado.matches(CEP_SEM_MASCARA)) {
            cepFormatado = cepFormatado.substring(0, 5) + "-" + cepFormatado.substring(5);
        }

        return new Endereco(
                cepFormatado,
                response.street() != null ? response.street() : "",
                "", 
                "", 
                response.neighborhood() != null ? response.neighborhood() : "",
                response.city() != null ? response.city() : "",
                response.state() != null ? response.state() : "",
                "", 
                "", 
                "", 
                "", 
                "", 
                "", 
                "BRASILAPI"
        );
    }
}
