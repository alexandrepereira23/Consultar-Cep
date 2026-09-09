package com.alexandre.consultacep.dto;

public record EnderecoDetalhadoResponse(
    String cep,
    String logradouro,
    String complemento,
    String unidade,
    String bairro,
    String cidade,
    String uf,
    String estado,
    String regiao,
    String codigoIbge,
    String ddd,
    String siafi,
    String gia,
    String localizacao,
    String fonte
) {
}

