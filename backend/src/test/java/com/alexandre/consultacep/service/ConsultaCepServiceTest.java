package com.alexandre.consultacep.service;


import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.dto.LocalizacaoResponse;
import com.alexandre.consultacep.exception.CepInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaCepServiceTest {

    private CachedProvedorCep provedorCep;
    private ConsultaCepService consultaCepService;

    @BeforeEach
    void setUp() {
        provedorCep = mock(CachedProvedorCep.class);
        consultaCepService = new ConsultaCepService(provedorCep);
    }

    @Test
    void deveRetornarEnderecoBasicoParaCepValido() {
        Endereco endereco = enderecoPadrao();
        when(provedorCep.consultar("01001000")).thenReturn(endereco);

        EnderecoBasicoResponse response = consultaCepService.consultarBasico("01001000");

        assertNotNull(response);
        assertEquals("01001-000", response.cep());
        assertEquals("Praça da Sé", response.logradouro());
        verify(provedorCep, times(1)).consultar("01001000");
    }

    @Test
    void deveAceitarCepComMascara() {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao());

        EnderecoBasicoResponse response = consultaCepService.consultarBasico("01001-000");

        assertEquals("01001-000", response.cep());
        verify(provedorCep).consultar("01001000");
    }

    @Test
    void deveRetornarEnderecoDetalhadoParaCepValido() {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao());

        EnderecoDetalhadoResponse response = consultaCepService.consultarDetalhado("01001000");

        assertNotNull(response);
        assertEquals("01001-000", response.cep());
        assertEquals("São Paulo", response.estado());
        assertNull(response.localizacao());
        assertEquals("VIACEP", response.fonte());
        verify(provedorCep, times(1)).consultar("01001000");
    }

    @Test
    void deveManterLocalizacaoTipadaENulaNestaFase() {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao());

        EnderecoDetalhadoResponse response = consultaCepService.consultarDetalhado("01001000");

        assertNull(response.localizacao());
        assertSame(LocalizacaoResponse.class, EnderecoDetalhadoResponse.class.getRecordComponents()[13].getType());
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

    @Test
    void deveLancarExcecaoParaCepComEspacos() {
        assertThrows(CepInvalidoException.class, () -> consultaCepService.consultarBasico(" 01001000 "));
    }

    @Test
    void deveConsultarEndpointDetalhadoComCepComMascara() {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao());

        EnderecoDetalhadoResponse response = consultaCepService.consultarDetalhado("01001-000");

        assertEquals("01001-000", response.cep());
        assertNull(response.localizacao());
        verify(provedorCep).consultar("01001000");
    }

    @Test
    void deveRetornarApenasCamposSolicitadosEPreservarOrdem() {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao());
        var resultado = consultaCepService.consultarCampos("01001000", "uf,cidade,cep");
        
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        
        var iterador = resultado.keySet().iterator();
        assertEquals("uf", iterador.next());
        assertEquals("cidade", iterador.next());
        assertEquals("cep", iterador.next());
        
        assertEquals("SP", resultado.get("uf"));
        assertEquals("São Paulo", resultado.get("cidade"));
        assertEquals("01001-000", resultado.get("cep"));
    }

    @Test
    void deveRemoverCamposDuplicados() {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao());
        var resultado = consultaCepService.consultarCampos("01001000", "cep,cep,cidade");
        
        assertEquals(2, resultado.size());
        assertTrue(resultado.containsKey("cep"));
        assertTrue(resultado.containsKey("cidade"));
    }

    @Test
    void deveAceitarEspacosNosCampos() {
        when(provedorCep.consultar("01001000")).thenReturn(enderecoPadrao());
        var resultado = consultaCepService.consultarCampos("01001000", " cep , logradouro,  cidade ");
        
        assertEquals(3, resultado.size());
        assertTrue(resultado.containsKey("cep"));
        assertTrue(resultado.containsKey("logradouro"));
        assertTrue(resultado.containsKey("cidade"));
    }

    @Test
    void deveLancarExcecaoParaCampoInvalido() {
        com.alexandre.consultacep.exception.CamposInvalidosException ex = assertThrows(
            com.alexandre.consultacep.exception.CamposInvalidosException.class, 
            () -> consultaCepService.consultarCampos("01001000", "cep,nomeRua,uf")
        );
        assertTrue(ex.getMessage().contains("Campo inválido solicitado: nomeRua."));
    }

    @Test
    void deveLancarExcecaoParaListaVazia() {
        assertThrows(
            com.alexandre.consultacep.exception.CamposInvalidosException.class, 
            () -> consultaCepService.consultarCampos("01001000", "")
        );
        assertThrows(
            com.alexandre.consultacep.exception.CamposInvalidosException.class, 
            () -> consultaCepService.consultarCampos("01001000", "   ")
        );
        assertThrows(
            com.alexandre.consultacep.exception.CamposInvalidosException.class, 
            () -> consultaCepService.consultarCampos("01001000", ",,,")
        );
    }

    @Test
    void deveManterValidacaoDeCepParaConsultaCampos() {
        assertThrows(
            CepInvalidoException.class, 
            () -> consultaCepService.consultarCampos("123", "cep")
        );
    }

    private Endereco enderecoPadrao() {
        return new Endereco("01001-000", "Praça da Sé", "lado ímpar", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", "VIACEP");
    }
}
