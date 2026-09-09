package com.alexandre.consultacep.service;

import com.alexandre.consultacep.client.ProvedorCep;
import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.exception.CepInvalidoException;
import org.springframework.stereotype.Service;

@Service
public class ConsultaCepService {

    private final ProvedorCep provedorCep;

    public ConsultaCepService(ProvedorCep provedorCep) {
        this.provedorCep = provedorCep;
    }

    public EnderecoBasicoResponse consultarBasico(String cep) {
        Endereco endereco = consultarEValidar(cep);
        return new EnderecoBasicoResponse(
                endereco.cep(),
                endereco.logradouro(),
                endereco.bairro(),
                endereco.cidade(),
                endereco.uf()
        );
    }

    public EnderecoDetalhadoResponse consultarDetalhado(String cep) {
        Endereco endereco = consultarEValidar(cep);
        return new EnderecoDetalhadoResponse(
                endereco.cep(),
                endereco.logradouro(),
                endereco.complemento(),
                endereco.unidade(),
                endereco.bairro(),
                endereco.cidade(),
                endereco.uf(),
                endereco.estado(),
                endereco.regiao(),
                endereco.codigoIbge(),
                endereco.ddd(),
                endereco.siafi(),
                endereco.gia(),
                null,
                endereco.fonte()
        );
    }

    private Endereco consultarEValidar(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new CepInvalidoException("O CEP não pode ser vazio.");
        }

        String cepNormalizado = cep.replace("-", "");

        if (!cepNormalizado.matches("\\d{8}")) {
            throw new CepInvalidoException("O CEP deve conter exatamente 8 números.");
        }
        
        if (!cep.matches("\\d{8}") && !cep.matches("\\d{5}-\\d{3}")) {
              throw new CepInvalidoException("Formato de CEP inválido. Use 00000000 ou 00000-000.");
        }

        return provedorCep.consultar(cepNormalizado);
    }
}
