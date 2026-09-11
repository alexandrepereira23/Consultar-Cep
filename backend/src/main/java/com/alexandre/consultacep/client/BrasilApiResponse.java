package com.alexandre.consultacep.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BrasilApiResponse(
        String cep,
        String state,
        String city,
        String neighborhood,
        String street,
        String service
) {}
