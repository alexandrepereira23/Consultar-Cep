package com.alexandre.consultacep.controller;

import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.exception.CepInvalidoException;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import com.alexandre.consultacep.service.ConsultaCepService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CepController.class)
class CepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaCepService service;

    @Test
    void deveRetornar200ComContratoEnxutoParaConsultaBasica() throws Exception {
        EnderecoBasicoResponse response = new EnderecoBasicoResponse("01001-000", "Praça da Sé", "Sé", "São Paulo", "SP");
        when(service.consultarBasico("01001000")).thenReturn(response);

        mockMvc.perform(get("/api/v1/ceps/01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"))
                .andExpect(jsonPath("$.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.complemento").doesNotExist());
    }

    @Test
    void deveRetornar200ComContratoCompletoParaConsultaDetalhada() throws Exception {
        EnderecoDetalhadoResponse response = new EnderecoDetalhadoResponse(
                "01001-000", "Praça da Sé", "lado ímpar", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", null, "VIACEP"
        );
        when(service.consultarDetalhado("01001-000")).thenReturn(response);

        mockMvc.perform(get("/api/v1/ceps/01001-000/detalhes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"))
                .andExpect(jsonPath("$.fonte").value("VIACEP"))
                .andExpect(jsonPath("$.localizacao").isEmpty());
    }

    @Test
    void deveRetornar400ParaCepInvalido() throws Exception {
        when(service.consultarBasico("01001-00A")).thenThrow(new CepInvalidoException("Formato de CEP invalido."));

        mockMvc.perform(get("/api/v1/ceps/01001-00A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("CEP_INVALIDO"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Formato de CEP invalido."));
    }

    @Test
    void deveRetornar404ParaCepInexistente() throws Exception {
        when(service.consultarBasico("99999999")).thenThrow(new CepNaoEncontradoException("O CEP 99999999 nao foi encontrado no ViaCEP."));

        mockMvc.perform(get("/api/v1/ceps/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("CEP_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveRetornar503ParaFornecedorIndisponivel() throws Exception {
        when(service.consultarBasico("01001000")).thenThrow(new ServicoIndisponivelException("O ViaCEP retornou status 500"));

        mockMvc.perform(get("/api/v1/ceps/01001000"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value("SERVICO_CEP_INDISPONIVEL"))
                .andExpect(jsonPath("$.status").value(503));
    }
}

