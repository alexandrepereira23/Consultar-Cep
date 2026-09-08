import { HttpErrorResponse, HttpStatusCode, HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, throwError } from 'rxjs';

import { Endereco } from '../models/endereco';
import { EnderecoViaCep } from '../models/endereco-via-cep';

export type CepErroTipo =
  | 'cep-invalido'
  | 'cep-nao-encontrado'
  | 'falha-conexao'
  | 'servico-indisponivel';

export class CepConsultaErro extends Error {
  constructor(readonly tipo: CepErroTipo) {
    super(tipo);
    this.name = 'CepConsultaErro';
  }
}

@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly http = inject(HttpClient);
  private readonly viaCepUrl = 'https://viacep.com.br/ws';

  buscar(cepInformado: string): Observable<Endereco> {
    const cep = this.normalizarCep(cepInformado);

    if (!this.cepValido(cep)) {
      return throwError(() => new CepConsultaErro('cep-invalido'));
    }

    return this.http.get<EnderecoViaCep>(`${this.viaCepUrl}/${cep}/json/`).pipe(
      map((resposta) => {
        if (resposta.erro === true || resposta.erro === 'true') {
          throw new CepConsultaErro('cep-nao-encontrado');
        }

        return this.mapearEndereco(resposta);
      }),
      catchError((erro: unknown) => throwError(() => this.mapearErro(erro))),
    );
  }

  normalizarCep(cep: string): string {
    return cep.replace(/\D/g, '');
  }

  cepValido(cep: string): boolean {
    return /^\d{8}$/.test(cep);
  }

  private mapearEndereco(resposta: EnderecoViaCep): Endereco {
    return {
      cep: resposta.cep ?? '',
      logradouro: resposta.logradouro ?? '',
      complemento: resposta.complemento ?? '',
      unidade: resposta.unidade ?? '',
      bairro: resposta.bairro ?? '',
      cidade: resposta.localidade ?? '',
      uf: resposta.uf ?? '',
      estado: resposta.estado ?? '',
      regiao: resposta.regiao ?? '',
      codigoIbge: resposta.ibge ?? '',
      gia: resposta.gia ?? '',
      ddd: resposta.ddd ?? '',
      siafi: resposta.siafi ?? '',
    };
  }

  private mapearErro(erro: unknown): CepConsultaErro {
    if (erro instanceof CepConsultaErro) {
      return erro;
    }

    if (erro instanceof HttpErrorResponse) {
      if (erro.status === 0) {
        return new CepConsultaErro('falha-conexao');
      }

      if (erro.status >= HttpStatusCode.InternalServerError) {
        return new CepConsultaErro('servico-indisponivel');
      }
    }

    return new CepConsultaErro('servico-indisponivel');
  }
}
