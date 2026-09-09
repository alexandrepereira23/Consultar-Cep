package com.alexandre.consultacep.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<ErroResposta> handleCepInvalido(CepInvalidoException ex, HttpServletRequest request) {
        logger.warn("CEP Invalido: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.BAD_REQUEST.value(),
            "CEP_INVALIDO",
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> handleCepNaoEncontrado(CepNaoEncontradoException ex, HttpServletRequest request) {
        logger.info("CEP Nao Encontrado: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.NOT_FOUND.value(),
            "CEP_NAO_ENCONTRADO",
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResposta> handleServicoIndisponivel(ServicoIndisponivelException ex, HttpServletRequest request) {
        logger.error("Servico Indisponivel: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "SERVICO_CEP_INDISPONIVEL",
            "O servico de consulta de CEP esta temporariamente indisponivel.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(erro);
    }

    @ExceptionHandler(RespostaInvalidaException.class)
    public ResponseEntity<ErroResposta> handleRespostaInvalida(RespostaInvalidaException ex, HttpServletRequest request) {
        logger.error("Resposta Invalida: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.BAD_GATEWAY.value(),
            "RESPOSTA_FORNECEDOR_INVALIDA",
            "O fornecedor de CEP retornou uma resposta invalida.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> handleGenerico(Exception ex, HttpServletRequest request) {
        logger.error("Erro Interno Inesperado", ex);
        ErroResposta erro = new ErroResposta(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "ERRO_INTERNO",
            "Ocorreu um erro interno inesperado.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}

