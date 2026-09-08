import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { EnderecoViaCep } from '../models/endereco-via-cep';
import { CepConsultaErro, CepService } from './cep.service';

describe('CepService', () => {
  let service: CepService;
  let http: HttpTestingController;

  const respostaViaCep: EnderecoViaCep = {
    cep: '01001-000',
    logradouro: 'Praca da Se',
    complemento: 'lado impar',
    unidade: '',
    bairro: 'Se',
    localidade: 'Sao Paulo',
    uf: 'SP',
    estado: 'Sao Paulo',
    regiao: 'Sudeste',
    ibge: '3550308',
    gia: '1004',
    ddd: '11',
    siafi: '7107',
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

  it('consulta o ViaCEP e mapeia resposta encontrada', () => {
    let enderecoRecebido: unknown;

    service.buscar('01001-000').subscribe((endereco) => enderecoRecebido = endereco);

    const requisicao = http.expectOne('https://viacep.com.br/ws/01001000/json/');
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(respostaViaCep);

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

  it('trata resposta com erro true como CEP nao encontrado', () => {
    let erroRecebido: unknown;

    service.buscar('99999-999').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('https://viacep.com.br/ws/99999999/json/');
    requisicao.flush({ erro: true });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('cep-nao-encontrado');
  });

  it('trata erro string true do ViaCEP como CEP nao encontrado', () => {
    let erroRecebido: unknown;

    service.buscar('99999-999').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('https://viacep.com.br/ws/99999999/json/');
    requisicao.flush({ erro: 'true' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('cep-nao-encontrado');
  });

  it('diferencia falha de conexao', () => {
    let erroRecebido: unknown;

    service.buscar('01001000').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('https://viacep.com.br/ws/01001000/json/');
    requisicao.flush(null, { status: 0, statusText: 'Unknown Error' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('falha-conexao');
  });

  it('diferencia indisponibilidade do servico em erro HTTP', () => {
    let erroRecebido: unknown;

    service.buscar('01001000').subscribe({ error: (erro: unknown) => erroRecebido = erro });

    const requisicao = http.expectOne('https://viacep.com.br/ws/01001000/json/');
    requisicao.flush(null, { status: 503, statusText: 'Service Unavailable' });

    expect(erroRecebido).toBeInstanceOf(CepConsultaErro);
    expect((erroRecebido as CepConsultaErro).tipo).toBe('servico-indisponivel');
  });
});
