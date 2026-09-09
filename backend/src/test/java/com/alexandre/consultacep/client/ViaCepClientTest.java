package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.RespostaInvalidaException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

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
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(respostaValidaCompleta(), MediaType.APPLICATION_JSON));

        Endereco endereco = viaCepClient.consultar("01001000");

        assertNotNull(endereco);
        assertEquals("01001-000", endereco.cep());
        assertEquals("Praça da Sé", endereco.logradouro());
        assertEquals("São Paulo", endereco.cidade());
        assertEquals("VIACEP", endereco.fonte());
    }

    @Test
    void deveRetornarEnderecoQuandoLogradouroEBairroVieremVazios() {
        String jsonResponse = """
                {
                  "cep": "01001-000",
                  "logradouro": "",
                  "complemento": "",
                  "unidade": "",
                  "bairro": "",
                  "localidade": "São Paulo",
                  "uf": "SP",
                  "estado": "São Paulo",
                  "regiao": "Sudeste",
                  "ibge": "3550308",
                  "gia": "",
                  "ddd": "11",
                  "siafi": "7107"
                }
                """;

        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Endereco endereco = viaCepClient.consultar("01001000");

        assertEquals("", endereco.logradouro());
        assertEquals("", endereco.bairro());
        assertEquals("São Paulo", endereco.cidade());
        assertEquals("SP", endereco.uf());
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
    void deveLancarRespostaInvalidaQuandoRespostaForNula() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarRespostaInvalidaQuandoJsonForMalformado() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess("{", MediaType.APPLICATION_JSON));

        assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarRespostaInvalidaQuandoCepEstiverAusente() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(respostaValidaCompleta().replace("\"cep\": \"01001-000\",", ""), MediaType.APPLICATION_JSON));

        assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarRespostaInvalidaQuandoCidadeEstiverAusente() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(respostaValidaCompleta().replace("\"localidade\": \"São Paulo\",", ""), MediaType.APPLICATION_JSON));

        assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarRespostaInvalidaQuandoUfEstiverAusente() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(respostaValidaCompleta().replace("\"uf\": \"SP\",", ""), MediaType.APPLICATION_JSON));

        assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarRespostaInvalidaQuandoUfForInvalida() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(respostaValidaCompleta().replace("\"uf\": \"SP\"", "\"uf\": \"SPO\""), MediaType.APPLICATION_JSON));

        assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarServicoIndisponivelQuandoServerError() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withServerError());

        assertThrows(ServicoIndisponivelException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarServicoIndisponivelQuandoFornecedorRetornarHttp5xx() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(ServicoIndisponivelException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarServicoIndisponivelQuandoOcorrerTimeout() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withException(new SocketTimeoutException("timeout")));

        assertThrows(ServicoIndisponivelException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void deveLancarServicoIndisponivelQuandoOcorrerFalhaDeConexao() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withException(new java.net.ConnectException("connection refused")));

        assertThrows(ServicoIndisponivelException.class, () -> viaCepClient.consultar("01001000"));
    }

    @Test
    void devePreservarExcecoesDeDominio() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        RespostaInvalidaException exception = assertThrows(RespostaInvalidaException.class, () -> viaCepClient.consultar("01001000"));

        assertFalse(exception.getMessage().contains("indispon"));
    }

    private String respostaValidaCompleta() {
        return """
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
    }
}
