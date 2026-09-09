package com.alexandre.consultacep.controller;

import com.alexandre.consultacep.dto.EnderecoBasicoResponse;
import com.alexandre.consultacep.dto.EnderecoDetalhadoResponse;
import com.alexandre.consultacep.exception.ErroResposta;
import com.alexandre.consultacep.service.ConsultaCepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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
    @Operation(summary = "Busca básica de endereço por CEP",
               description = "Retorna os dados principais de um endereço. Formatos aceitos: 00000000 e 00000-000.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnderecoBasicoResponse.class), examples = @ExampleObject(name = "Resposta básica", value = """
                    {
                      "cep": "01001-000",
                      "logradouro": "Praça da Sé",
                      "bairro": "Sé",
                      "cidade": "São Paulo",
                      "uf": "SP"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "CEP inválido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(name = "Erro padronizado", value = """
                    {
                      "status": 400,
                      "erro": "CEP_INVALIDO",
                      "mensagem": "O CEP informado é inválido. Use o formato 00000000 ou 00000-000.",
                      "caminho": "/api/v1/ceps/123",
                      "timestamp": "2026-09-09T12:00:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "CEP não encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "502", description = "Resposta inválida do serviço de consulta de CEP.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "503", description = "Serviço de consulta de CEP temporariamente indisponível.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EnderecoBasicoResponse> consultarBasico(
            @Parameter(description = "CEP a ser consultado. Exemplos: 01001000 ou 01001-000.", example = "01001-000")
            @PathVariable String cep) {
        return ResponseEntity.ok(service.consultarBasico(cep));
    }

    @GetMapping("/{cep}/detalhes")
    @Operation(summary = "Busca detalhada de endereço por CEP",
               description = "Retorna os dados detalhados de um endereço, incluindo região, código IBGE, DDD e localização nula nesta fase. Formatos aceitos: 00000000 e 00000-000.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnderecoDetalhadoResponse.class), examples = @ExampleObject(name = "Resposta detalhada", value = """
                    {
                      "cep": "01001-000",
                      "logradouro": "Praça da Sé",
                      "complemento": "lado ímpar",
                      "unidade": "",
                      "bairro": "Sé",
                      "cidade": "São Paulo",
                      "uf": "SP",
                      "estado": "São Paulo",
                      "regiao": "Sudeste",
                      "codigoIbge": "3550308",
                      "ddd": "11",
                      "siafi": "7107",
                      "gia": "1004",
                      "localizacao": null,
                      "fonte": "VIACEP"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "CEP inválido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "CEP não encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "502", description = "Resposta inválida do serviço de consulta de CEP.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "503", description = "Serviço de consulta de CEP temporariamente indisponível.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EnderecoDetalhadoResponse> consultarDetalhado(
            @Parameter(description = "CEP a ser consultado. Exemplos: 01001000 ou 01001-000.", example = "01001-000")
            @PathVariable String cep) {
        return ResponseEntity.ok(service.consultarDetalhado(cep));
    }
}
