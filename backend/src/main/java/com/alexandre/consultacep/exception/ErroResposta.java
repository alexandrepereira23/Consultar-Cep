package com.alexandre.consultacep.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resposta padronizada de erro da API.")
public record ErroResposta(
    @Schema(description = "Status HTTP retornado.", example = "404")
    int status,

    @Schema(description = "Código estável do erro.", example = "CEP_NAO_ENCONTRADO")
    String erro,

    @Schema(description = "Mensagem pública segura para o consumidor da API.", example = "O CEP informado não foi encontrado.")
    String mensagem,

    @Schema(description = "Caminho da requisição que causou o erro.", example = "/api/v1/ceps/99999999")
    String caminho,

    @Schema(description = "Instante em que o erro foi gerado.", example = "2026-09-09T12:00:00Z")
    Instant timestamp
) {
}
