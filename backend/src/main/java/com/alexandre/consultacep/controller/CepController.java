package com.alexandre.consultacep.controller;

import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.service.ConsultaCepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ceps")
@Tag(name = "Consulta de CEP", description = "Endpoints para buscar endereços a partir de um CEP")
public class CepController {

    private final ConsultaCepService service;

    public CepController(ConsultaCepService service) {
        this.service = service;
    }

    @GetMapping("/{cep}")
    @Operation(summary = "Busca basica de endereco por CEP", 
               description = "Retorna os dados principais de um endereco. Formatos aceitos: 00000000 ou 00000-000")
    public ResponseEntity<EnderecoBasicoResponse> consultarBasico(
            @Parameter(description = "CEP a ser consultado", example = "01001-000") 
            @PathVariable String cep) {
        return ResponseEntity.ok(service.consultarBasico(cep));
    }

    @GetMapping("/{cep}/detalhes")
    @Operation(summary = "Busca detalhada de endereco por CEP", 
               description = "Retorna os dados detalhados de um endereco, incluindo regiao, ibge, ddd, etc. Formatos aceitos: 00000000 ou 00000-000")
    public ResponseEntity<EnderecoDetalhadoResponse> consultarDetalhado(
            @Parameter(description = "CEP a ser consultado", example = "01001-000") 
            @PathVariable String cep) {
        return ResponseEntity.ok(service.consultarDetalhado(cep));
    }
}

