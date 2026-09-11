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

    @ExceptionHandler(CamposInvalidosException.class)
    public ResponseEntity<ErroResposta> handleCamposInvalidos(CamposInvalidosException ex, HttpServletRequest request) {
        logger.warn("Campos inválidos: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.BAD_REQUEST.value(),
            "CAMPOS_INVALIDOS",
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<ErroResposta> handleCepInvalido(CepInvalidoException ex, HttpServletRequest request) {
        logger.warn("CEP inválido: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.BAD_REQUEST.value(),
            "CEP_INVALIDO",
            "O CEP informado é inválido. Use o formato 00000000 ou 00000-000.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> handleCepNaoEncontrado(CepNaoEncontradoException ex, HttpServletRequest request) {
        logger.info("CEP não encontrado: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.NOT_FOUND.value(),
            "CEP_NAO_ENCONTRADO",
            "O CEP informado não foi encontrado.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResposta> handleServicoIndisponivel(ServicoIndisponivelException ex, HttpServletRequest request) {
        logger.error("Serviço de CEP indisponível: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "SERVICO_CEP_INDISPONIVEL",
            "O serviço de consulta de CEP está temporariamente indisponível.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(erro);
    }

    @ExceptionHandler(RespostaInvalidaException.class)
    public ResponseEntity<ErroResposta> handleRespostaInvalida(RespostaInvalidaException ex, HttpServletRequest request) {
        logger.error("Resposta inválida do fornecedor de CEP: {}", ex.getMessage());
        ErroResposta erro = new ErroResposta(
            HttpStatus.BAD_GATEWAY.value(),
            "RESPOSTA_FORNECEDOR_INVALIDA",
            "O serviço de consulta de CEP retornou uma resposta inválida.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(erro);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> handleMissingParams(org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.warn("Parâmetro ausente: {}", ex.getParameterName());
        ErroResposta erro = new ErroResposta(
            HttpStatus.BAD_REQUEST.value(),
            "PARAMETRO_AUSENTE",
            "O parâmetro obrigatório '" + ex.getParameterName() + "' não foi informado.",
            request.getRequestURI(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> handleGenerico(Exception ex, HttpServletRequest request) {
        logger.error("Erro interno inesperado", ex);
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
