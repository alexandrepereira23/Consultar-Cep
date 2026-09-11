package com.alexandre.consultacep.service;

import com.alexandre.consultacep.client.ProvedorCep;
import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.exception.CepNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class ConsultaCepCacheIntegrationTest {

    @Autowired
    private ConsultaCepService consultaCepService;

    // Use MockitoBean to replace the ProvedorCep implementation inside CachedProvedorCep
    @MockitoBean
    private ProvedorCep provedorCepMock;

    @Test
    void deveCachearConsultaBemSucedida() {
        Endereco endereco = new Endereco("01001-000", "Praça da Sé", "", "", "Sé", "São Paulo", "SP", "São Paulo", "Sudeste", "3550308", "11", "7107", "1004", "VIACEP");
        
        when(provedorCepMock.consultar("01001000")).thenReturn(endereco);

        // Primeira chamada (basico)
        consultaCepService.consultarBasico("01001000");
        
        // Segunda chamada (detalhado) com máscara (deve limpar máscara e bater no mesmo cache)
        consultaCepService.consultarDetalhado("01001-000");
        
        // Terceira chamada
        consultaCepService.consultarBasico("01001-000");

        // O provedor externo (ViaCEP) só deve ter sido chamado uma única vez!
        verify(provedorCepMock, times(1)).consultar("01001000");
    }

    @Test
    void naoDeveCachearErro() {
        when(provedorCepMock.consultar("99999999"))
                .thenThrow(new CepNaoEncontradoException("CEP não encontrado"));

        // Primeira chamada deve lançar exceção
        assertThrows(CepNaoEncontradoException.class, () -> consultaCepService.consultarBasico("99999999"));
        
        // Segunda chamada deve lançar exceção novamente e chamar provedor
        assertThrows(CepNaoEncontradoException.class, () -> consultaCepService.consultarBasico("99999999"));

        verify(provedorCepMock, times(2)).consultar("99999999");
    }
}
