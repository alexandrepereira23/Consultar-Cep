package com.alexandre.consultacep.exception;

import java.time.Instant;

public record ErroResposta(
    int status,
    String erro,
    String mensagem,
    String caminho,
    Instant timestamp
) {
}

