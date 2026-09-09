package com.alexandre.consultacep.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta detalhada da consulta de CEP.")
public record EnderecoDetalhadoResponse(
    @Schema(description = "CEP no formato 00000-000.", example = "01001-000")
    String cep,

    @Schema(description = "Logradouro associado ao CEP. Pode estar vazio em CEPs gerais de municípios.", example = "Praça da Sé")
    String logradouro,

    @Schema(description = "Complemento retornado pelo fornecedor de dados.", example = "lado ímpar")
    String complemento,

    @Schema(description = "Unidade retornada pelo fornecedor de dados.", example = "")
    String unidade,

    @Schema(description = "Bairro associado ao CEP. Pode estar vazio em CEPs gerais de municípios.", example = "Sé")
    String bairro,

    @Schema(description = "Cidade ou localidade associada ao CEP.", example = "São Paulo")
    String cidade,

    @Schema(description = "Unidade federativa com duas letras.", example = "SP")
    String uf,

    @Schema(description = "Nome do estado.", example = "São Paulo")
    String estado,

    @Schema(description = "Região do Brasil.", example = "Sudeste")
    String regiao,

    @Schema(description = "Código IBGE da localidade.", example = "3550308")
    String codigoIbge,

    @Schema(description = "Código DDD da localidade.", example = "11")
    String ddd,

    @Schema(description = "Código SIAFI da localidade.", example = "7107")
    String siafi,

    @Schema(description = "Código GIA da localidade, quando disponível.", example = "1004")
    String gia,

    @Schema(description = "Coordenadas geográficas. Nesta fase permanece nulo porque o ViaCEP não fornece latitude e longitude.", nullable = true)
    LocalizacaoResponse localizacao,

    @Schema(description = "Fonte interna usada para montar os dados.", example = "VIACEP")
    String fonte
) {
}
