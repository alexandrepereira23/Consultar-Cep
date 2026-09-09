package com.alexandre.consultacep.client;

import com.alexandre.consultacep.domain.Endereco;

public interface ProvedorCep {
    Endereco consultar(String cep);
}

