package com.alexandre.consultacep.service;

import com.alexandre.consultacep.client.ProvedorCep;
import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.exception.CepInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaCepServiceTest {

    private ProvedorCep provedorCep;
    private ConsultaCepService consultaCepService;

    @BeforeEach
    void setUp() {
        provedorCep = mock(ProvedorCep.class);
        consultaCepService = new ConsultaCepService(provedorCep);
    }

    @Test
    void deveRetornarEnderecoBasicoParaCepValido() {
        Endereco endereco = new Endereco("01001-000", "Praça da Sé", "lado ímpar", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", "VIACEP");
        when(provedorCep.consultar("01001000")).thenReturn(endereco);

        EnderecoBasicoResponse response = consultaCepService.consultarBasico("01001-000");

        assertNotNull(response);
        assertEquals("01001-000", response.cep());
        assertEquals("Praça da Sé", response.logradouro());
        verify(provedorCep, times(1)).consultar("01001000");
    }

    @Test
    void deveRetornarEnderecoDetalhadoParaCepValido() {
        Endereco endereco = new Endereco("01001-000", "Praça da Sé", "lado ímpar", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", "VIACEP");
        when(provedorCep.consultar("01001000")).thenReturn(endereco);

        EnderecoDetalhadoResponse response = consultaCepService.consultarDetalhado("01001000");

        assertNotNull(response);
        assertEquals("01001-000", response.cep());
        assertEquals("São Paulo", response.estado());
        assertNull(response.localizacao());
        assertEquals("VIACEP", response.fonte());
        verify(provedorCep, times(1)).consultar("01001000");
    }

    @Test
    void deveLancarExcecaoParaCepVazio() {
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico(""));
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico(null));
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico("   "));
    }

    @Test
    void deveLancarExcecaoParaCepIncompleto() {
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico("1234567"));
    }

    @Test
    void deveLancarExcecaoParaCepComNoveNumeros() {
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico("123456789"));
    }

    @Test
    void deveLancarExcecaoParaCepComLetras() {
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico("01001-00A"));
    }

    @Test
    void deveLancarExcecaoParaCepComMascaraInvalida() {
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico("0100-1000"));
    }
}

