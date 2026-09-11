package com.alexandre.consultacep.service;

import com.alexandre.consultacep.client.ProvedorCep;
import com.alexandre.consultacep.domain.Endereco;
import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.exception.CepInvalidoException;
import com.alexandre.consultacep.exception.CamposInvalidosException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConsultaCepService {

    private final CachedProvedorCep provedorCep;

    public ConsultaCepService(CachedProvedorCep provedorCep) {
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

    private static final Set<String> CAMPOS_PERMITIDOS = Set.of(
            "cep", "logradouro", "complemento", "unidade", "bairro", "cidade", "uf", "estado",
            "regiao", "codigoIbge", "ddd", "siafi", "gia", "localizacao", "fonte"
    );

    public Map<String, Object> consultarCampos(String cep, String campos) {
        if (campos == null || campos.trim().isEmpty()) {
            throw new CamposInvalidosException("A lista de campos não pode ser vazia.");
        }

        List<String> camposSolicitados = Arrays.stream(campos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (camposSolicitados.isEmpty()) {
            throw new CamposInvalidosException("A lista de campos não pode ser vazia.");
        }

        List<String> camposInvalidos = camposSolicitados.stream()
                .filter(campo -> !CAMPOS_PERMITIDOS.contains(campo))
                .toList();

        if (!camposInvalidos.isEmpty()) {
            if (camposInvalidos.size() == 1) {
                throw new CamposInvalidosException("Campo inválido solicitado: " + camposInvalidos.get(0) + ".");
            } else {
                throw new CamposInvalidosException("Campos inválidos solicitados: " + String.join(", ", camposInvalidos) + ".");
            }
        }

        EnderecoDetalhadoResponse detalhado = consultarDetalhado(cep);
        Map<String, Object> resultado = new LinkedHashMap<>();

        for (String campo : camposSolicitados) {
            switch (campo) {
                case "cep" -> resultado.put("cep", detalhado.cep());
                case "logradouro" -> resultado.put("logradouro", detalhado.logradouro());
                case "complemento" -> resultado.put("complemento", detalhado.complemento());
                case "unidade" -> resultado.put("unidade", detalhado.unidade());
                case "bairro" -> resultado.put("bairro", detalhado.bairro());
                case "cidade" -> resultado.put("cidade", detalhado.cidade());
                case "uf" -> resultado.put("uf", detalhado.uf());
                case "estado" -> resultado.put("estado", detalhado.estado());
                case "regiao" -> resultado.put("regiao", detalhado.regiao());
                case "codigoIbge" -> resultado.put("codigoIbge", detalhado.codigoIbge());
                case "ddd" -> resultado.put("ddd", detalhado.ddd());
                case "siafi" -> resultado.put("siafi", detalhado.siafi());
                case "gia" -> resultado.put("gia", detalhado.gia());
                case "localizacao" -> resultado.put("localizacao", detalhado.localizacao());
                case "fonte" -> resultado.put("fonte", detalhado.fonte());
            }
        }

        return resultado;
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
