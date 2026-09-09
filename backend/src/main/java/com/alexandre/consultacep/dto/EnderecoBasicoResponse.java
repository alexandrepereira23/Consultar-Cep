package com.alexandre.consultacep.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta básica da consulta de CEP.")
public record EnderecoBasicoResponse(
    @Schema(description = "CEP no formato 00000-000.", example = "01001-000")
    String cep,

    @Schema(description = "Logradouro associado ao CEP. Pode estar vazio em CEPs gerais de municípios.", example = "Praça da Sé")
    String logradouro,

    @Schema(description = "Bairro associado ao CEP. Pode estar vazio em CEPs gerais de municípios.", example = "Sé")
    String bairro,

    @Schema(description = "Cidade ou localidade associada ao CEP.", example = "São Paulo")
    String cidade,

    @Schema(description = "Unidade federativa com duas letras.", example = "SP")
    String uf
) {
}
