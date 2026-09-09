package com.alexandre.consultacep.domain;

public record Endereco(
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
    String fonte
) {
}

