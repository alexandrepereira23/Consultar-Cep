package com.alexandre.consultacep.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Coordenadas geográficas do endereço, quando disponíveis pelo fornecedor de dados.")
public record LocalizacaoResponse(
    @Schema(description = "Latitude do endereço.", example = "-23.55052")
    Double latitude,

    @Schema(description = "Longitude do endereço.", example = "-46.633308")
    Double longitude
) {
}
