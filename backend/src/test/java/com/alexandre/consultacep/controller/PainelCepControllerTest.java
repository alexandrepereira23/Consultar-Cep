package com.alexandre.consultacep.controller;

import com.alexandre.consultacep.config.WebConfig;
import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.exception.CepInvalidoException;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.GlobalExceptionHandler;
import com.alexandre.consultacep.exception.RespostaInvalidaException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import com.alexandre.consultacep.security.ApiKeyProperties;
import com.alexandre.consultacep.security.RateLimitProperties;
import com.alexandre.consultacep.service.ConsultaCepService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PainelCepController.class)
@Import({GlobalExceptionHandler.class, WebConfig.class, ApiKeyProperties.class, RateLimitProperties.class})
@TestPropertySource(properties = "aplicacao.cors.origens-permitidas=http://localhost:4200")
class PainelCepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaCepService service;

    @Test
    void deveRetornar200ComContratoEnxutoParaConsultaBasicaDoPainel() throws Exception {
        when(service.consultarBasico("01001000")).thenReturn(respostaBasica());

        mockMvc.perform(get("/api/v1/painel/ceps/01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"))
                .andExpect(jsonPath("$.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.complemento").doesNotExist());
    }

    @Test
    void deveRetornar200ComContratoCompletoParaConsultaDetalhadaDoPainel() throws Exception {
        when(service.consultarDetalhado("01001-000")).thenReturn(respostaDetalhada());

        mockMvc.perform(get("/api/v1/painel/ceps/01001-000/detalhes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"))
                .andExpect(jsonPath("$.fonte").value("VIACEP"))
                .andExpect(jsonPath("$.localizacao").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void deveRetornar400ParaCepInvalidoNoPainel() throws Exception {
        when(service.consultarBasico("01001-00A")).thenThrow(new CepInvalidoException("Mensagem interna"));

        mockMvc.perform(get("/api/v1/painel/ceps/01001-00A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("CEP_INVALIDO"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("O CEP informado é inválido. Use o formato 00000000 ou 00000-000."))
                .andExpect(jsonPath("$.caminho").value("/api/v1/painel/ceps/01001-00A"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void deveRetornar404ParaCepInexistenteNoPainel() throws Exception {
        when(service.consultarBasico("99999999")).thenThrow(new CepNaoEncontradoException("O CEP 99999999 não foi encontrado no ViaCEP."));

        mockMvc.perform(get("/api/v1/painel/ceps/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("CEP_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("O CEP informado não foi encontrado."));
    }

    @Test
    void deveRetornarErrosPadronizadosNosDoisEndpointsDoPainel() throws Exception {
        when(service.consultarDetalhado("123")).thenThrow(new CepInvalidoException("Mensagem interna"));
        when(service.consultarDetalhado("99999999")).thenThrow(new CepNaoEncontradoException("ViaCEP interno"));
        when(service.consultarDetalhado("01001000")).thenThrow(new RespostaInvalidaException("JSON interno"));
        when(service.consultarDetalhado("02002000")).thenThrow(new ServicoIndisponivelException("Conexão recusada"));

        mockMvc.perform(get("/api/v1/painel/ceps/123/detalhes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("CEP_INVALIDO"));
        mockMvc.perform(get("/api/v1/painel/ceps/99999999/detalhes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("CEP_NAO_ENCONTRADO"));
        mockMvc.perform(get("/api/v1/painel/ceps/01001000/detalhes"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.erro").value("RESPOSTA_FORNECEDOR_INVALIDA"));
        mockMvc.perform(get("/api/v1/painel/ceps/02002000/detalhes"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value("SERVICO_CEP_INDISPONIVEL"));
    }

    @Test
    void devePermitirCorsGetParaOrigemLocalNoPainel() throws Exception {
        when(service.consultarBasico("01001000")).thenReturn(respostaBasica());

        mockMvc.perform(get("/api/v1/painel/ceps/01001000")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    void devePermitirPreflightOptionsParaOrigemLocalNoPainel() throws Exception {
        mockMvc.perform(options("/api/v1/painel/ceps/01001000")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("OPTIONS")));
    }

    private EnderecoBasicoResponse respostaBasica() {
        return new EnderecoBasicoResponse("01001-000", "Praça da Sé", "Sé", "São Paulo", "SP");
    }

    private EnderecoDetalhadoResponse respostaDetalhada() {
        return new EnderecoDetalhadoResponse(
                "01001-000", "Praça da Sé", "lado ímpar", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", null, "VIACEP"
        );
    }
}
