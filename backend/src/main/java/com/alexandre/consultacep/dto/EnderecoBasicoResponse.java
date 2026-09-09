package com.alexandre.consultacep.dto;

public record EnderecoBasicoResponse(
    String cep,
    String logradouro,
    String bairro,
    String cidade,
    String uf
) {
}

