package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import com.alexandre.consultacep.exception.RespostaInvalidaException;
import com.alexandre.consultacep.exception.ServicoIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvedorCepComFallbackTest {

    @Mock
    private ViaCepClient viaCepClient;

    @Mock
    private BrasilApiCepClient brasilApiCepClient;

    @InjectMocks
    private ProvedorCepComFallback provedorCepComFallback;

    private Endereco enderecoViaCep;
    private Endereco enderecoBrasilApi;

    @BeforeEach
    void setUp() {
        enderecoViaCep = new Endereco("01001-000", "Praça da Sé", "", "", "Sé", "São Paulo", "SP", "", "", "", "", "", "", "VIACEP");
        enderecoBrasilApi = new Endereco("01001-000", "Praça da Sé", "", "", "Sé", "São Paulo", "SP", "", "", "", "", "", "", "BRASILAPI");
    }

    @Test
    void quandoViaCepRetornaSucesso_brasilApiNaoDeveSerChamada() {
        when(viaCepClient.consultar(anyString())).thenReturn(enderecoViaCep);

        Endereco resultado = provedorCepComFallback.consultar("01001000");

        assertEquals("VIACEP", resultado.fonte());
        verify(viaCepClient).consultar("01001000");
        verifyNoInteractions(brasilApiCepClient);
    }

    @Test
    void quandoViaCepRetornaCepNaoEncontrado_brasilApiNaoDeveSerChamada() {
        when(viaCepClient.consultar(anyString())).thenThrow(new CepNaoEncontradoException("Não encontrado"));

        assertThrows(CepNaoEncontradoException.class, () -> provedorCepComFallback.consultar("01001000"));

        verify(viaCepClient).consultar("01001000");
        verifyNoInteractions(brasilApiCepClient);
    }

    @Test
    void quandoViaCepLancaServicoIndisponivel_brasilApiDeveSerChamadaERetornarSucesso() {
        when(viaCepClient.consultar(anyString())).thenThrow(new ServicoIndisponivelException("Indisponível"));
        when(brasilApiCepClient.consultar(anyString())).thenReturn(enderecoBrasilApi);

        Endereco resultado = provedorCepComFallback.consultar("01001000");

        assertEquals("BRASILAPI", resultado.fonte());
        verify(viaCepClient).consultar("01001000");
        verify(brasilApiCepClient).consultar("01001000");
    }

    @Test
    void quandoViaCepLancaRespostaInvalida_brasilApiDeveSerChamadaERetornarSucesso() {
        when(viaCepClient.consultar(anyString())).thenThrow(new RespostaInvalidaException("Inválida"));
        when(brasilApiCepClient.consultar(anyString())).thenReturn(enderecoBrasilApi);

        Endereco resultado = provedorCepComFallback.consultar("01001000");

        assertEquals("BRASILAPI", resultado.fonte());
        verify(viaCepClient).consultar("01001000");
        verify(brasilApiCepClient).consultar("01001000");
    }

    @Test
    void quandoAmbosFalhamPorIndisponibilidade_deveRetornarServicoIndisponivel() {
        when(viaCepClient.consultar(anyString())).thenThrow(new ServicoIndisponivelException("Indisponível"));
        when(brasilApiCepClient.consultar(anyString())).thenThrow(new ServicoIndisponivelException("Indisponível BRASILAPI"));

        assertThrows(ServicoIndisponivelException.class, () -> provedorCepComFallback.consultar("01001000"));

        verify(viaCepClient).consultar("01001000");
        verify(brasilApiCepClient).consultar("01001000");
    }

    @Test
    void quandoViaCepFalhaEBrasilApiRetornaRespostaInvalida_deveRetornarRespostaInvalida() {
        when(viaCepClient.consultar(anyString())).thenThrow(new ServicoIndisponivelException("Indisponível"));
        when(brasilApiCepClient.consultar(anyString())).thenThrow(new RespostaInvalidaException("Inválida BRASILAPI"));

        assertThrows(RespostaInvalidaException.class, () -> provedorCepComFallback.consultar("01001000"));

        verify(viaCepClient).consultar("01001000");
        verify(brasilApiCepClient).consultar("01001000");
    }

    @Test
    void quandoViaCepFalhaEBrasilApiRetornaCepNaoEncontrado_deveRetornarCepNaoEncontrado() {
        when(viaCepClient.consultar(anyString())).thenThrow(new ServicoIndisponivelException("Indisponível"));
        when(brasilApiCepClient.consultar(anyString())).thenThrow(new CepNaoEncontradoException("Não encontrado"));

        assertThrows(CepNaoEncontradoException.class, () -> provedorCepComFallback.consultar("01001000"));

        verify(viaCepClient).consultar("01001000");
        verify(brasilApiCepClient).consultar("01001000");
    }

    @Test
    void quandoViaCepLancaRuntimeException_brasilApiNaoDeveSerChamada_eExcecaoDeveSerPropagada() {
        when(viaCepClient.consultar(anyString())).thenThrow(new RuntimeException("Erro inesperado"));

        assertThrows(RuntimeException.class, () -> provedorCepComFallback.consultar("01001000"));

        verify(viaCepClient).consultar("01001000");
        verifyNoInteractions(brasilApiCepClient);
    }
}
