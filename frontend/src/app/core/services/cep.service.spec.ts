import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { EnderecoApiResponse } from '../models/endereco-api-response';
import { CepConsultaErro, CepService } from './cep.service';

describe('CepService', () => {
  let service: CepService;
  let http: HttpTestingController;

  const respostaApi: EnderecoApiResponse = {
    cep: '01001-000',
    logradouro: 'Praca da Se',
    complemento: 'lado impar',
    unidade: '',
    bairro: 'Se',
    cidade: 'Sao Paulo',
    uf: 'SP',
    estado: 'Sao Paulo',
    regiao: 'Sudeste',
    codigoIbge: '3550308',
    gia: '1004',
    ddd: '11',
    siafi: '7107',
    localizacao: null,
    fonte: 'Correios',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CepService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('normaliza CEP com mascara', () => {
    expect(service.normalizarCep('01001-000')).toBe('01001000');
  });

  it('rejeita CEP invalido sem chamar HTTP', () => {
    let erroRecebido: unknown;

    service.buscar('12345').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('cep-invalido');
  });

  it('consulta a API propria e mapeia resposta detalhada', () => {
    let enderecoRecebido: unknown;

    service.buscar('01001-000').subscribe((endereco) => enderecoRecebido = endereco);

    const requisicao = http.expectOne('http://localhost:8080/api/v1/ceps/01001000/detalhes');
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(respostaApi);

    expect(enderecoRecebido).toEqual({
      cep: '01001-000',
      logradouro: 'Praca da Se',
      complemento: 'lado impar',
      unidade: '',
      bairro: 'Se',
      cidade: 'Sao Paulo',
      uf: 'SP',
      estado: 'Sao Paulo',
      regiao: 'Sudeste',
      codigoIbge: '3550308',
      gia: '1004',
      ddd: '11',
      siafi: '7107',
    });
  });

  it('trata 400 como CEP invalido', () => {
    let erroRecebido: unknown;

    service.buscar('12345678').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('http://localhost:8080/api/v1/ceps/12345678/detalhes');
    requisicao.flush(null, { status: 400, statusText: 'Bad Request' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('cep-invalido');
  });

  it('trata 404 como CEP nao encontrado', () => {
    let erroRecebido: unknown;

    service.buscar('99999-999').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('http://localhost:8080/api/v1/ceps/99999999/detalhes');
    requisicao.flush(null, { status: 404, statusText: 'Not Found' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('cep-nao-encontrado');
  });

  it('diferencia falha de conexao', () => {
    let erroRecebido: unknown;

    service.buscar('01001000').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('http://localhost:8080/api/v1/ceps/01001000/detalhes');
    requisicao.flush(null, { status: 0, statusText: 'Unknown Error' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('falha-conexao');
  });

  it('trata 500 como erro interno', () => {
    let erroRecebido: unknown;

    service.buscar('01001000').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('http://localhost:8080/api/v1/ceps/01001000/detalhes');
    requisicao.flush(null, { status: 500, statusText: 'Internal Server Error' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('servico-indisponivel');
  });

  it('diferencia indisponibilidade do servico em erro HTTP 503', () => {
    let erroRecebido: unknown;

    service.buscar('01001000').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('http://localhost:8080/api/v1/ceps/01001000/detalhes');
    requisicao.flush(null, { status: 503, statusText: 'Service Unavailable' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('servico-indisponivel');
  });

  it('trata 502 como servico indisponivel', () => {
    let erroRecebido: unknown;

    service.buscar('01001000').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('http://localhost:8080/api/v1/ceps/01001000/detalhes');
    requisicao.flush(null, { status: 502, statusText: 'Bad Gateway' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('servico-indisponivel');
  });
});
