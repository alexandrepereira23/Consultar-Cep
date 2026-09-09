package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.RespostaInvalidaException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ViaCepClientTest {

    private ViaCepClient viaCepClient;
    private MockRestServiceServer server;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("https://viacep.com.br/ws").build();
        viaCepClient = new ViaCepClient(restClient);
    }

    @Test
    void deveRetornarEnderecoQuandoCepExistir() {
        String jsonResponse = """
                {
                  "cep": "01001-000",
                  "logradouro": "Praça da Sé",
                  "complemento": "lado ímpar",
                  "unidade": "",
                  "bairro": "Sé",
                  "localidade": "São Paulo",
                  "uf": "SP",
                  "estado": "São Paulo",
                  "regiao": "Sudeste",
                  "ibge": "3550308",
                  "gia": "1004",
                  "ddd": "11",
                  "siafi": "7107"
                }
                """;

        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Endereco endereco = viaCepClient.consultar("01001000");

        assertNotNull(endereco);
        assertEquals("01001-000", endereco.cep());
        assertEquals("Praça da Sé", endereco.logradouro());
        assertEquals("São Paulo", endereco.cidade());
        assertEquals("VIACEP", endereco.fonte());
    }

    @Test
    void deveLancarCepNaoEncontradoQuandoViacepRetornarErroTrue() {
        String jsonResponse = """
                {
                  "erro": true
                }
                """;

        server.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        assertThrows(CepNaoEncontradoException.class, () -> viaCepClient.consultar("99999999"));
    }

    @Test
    void deveLancarRespostaInvalidaQuandoBadRequest() {
        server.expect(requestTo("https://viacep.com.br/ws/123/json/"))
                .andRespond(withBadRequest());

        assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("123"));
    }

    @Test
    void deveLancarServicoIndisponivelQuandoServerError() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withServerError());

        assertThrows(ServicoIndisponivelException.class, () -> viaCepClient.consultar("01001000"));
    }
}

