import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';

import { Endereco } from '../../core/models/endereco';
import { CepConsultaErro, CepService } from '../../core/services/cep.service';
import { ConsultaCepComponent } from './consulta-cep';

class CepServiceStub {
  resposta$: Observable<Endereco> = of(enderecoPadrao);
  chamadas: string[] = [];

  buscar(cep: string): Observable<Endereco> {
    this.chamadas.push(cep);
    return this.resposta$;
  }
}

const enderecoPadrao: Endereco = {
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
};

describe('ConsultaCepComponent', () => {
  let fixture: ComponentFixture<ConsultaCepComponent>;
  let component: ConsultaCepComponent;
  let cepService: CepServiceStub;

  beforeEach(async () => {
    cepService = new CepServiceStub();

    await TestBed.configureTestingModule({
      imports: [ConsultaCepComponent],
      providers: [{ provide: CepService, useValue: cepService }],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultaCepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('inicia com formulario invalido', () => {
    expect(component.formulario.invalid).toBe(true);
    expect(botaoBuscar().disabled).toBe(true);
  });

  it('mantem busca desabilitada com CEP incompleto', () => {
    preencherCep('12345');

    expect(component.formulario.invalid).toBe(true);
    expect(botaoBuscar().disabled).toBe(true);
  });

  it('preenche os campos automaticamente apos sucesso', () => {
    preencherCep('01001-000');
    botaoBuscar().click();
    fixture.detectChanges();

    expect(cepService.chamadas).toEqual(['01001-000']);
    expect(component.formulario.controls.logradouro.value).toBe('Praca da Se');
    expect(component.formulario.controls.bairro.value).toBe('Se');
    expect(component.formulario.controls.cidade.value).toBe('Sao Paulo');
    expect(component.formulario.controls.uf.value).toBe('SP');
    expect(textoTela()).toContain('CEP 01001-000 encontrado');
  });

  it('mostra mensagem de CEP nao encontrado', () => {
    cepService.resposta$ = throwError(() => new CepConsultaErro('cep-nao-encontrado'));

    preencherCep('99999-999');
    botaoBuscar().click();
    fixture.detectChanges();

    expect(textoTela()).toContain('CEP não encontrado');
  });

  it('mostra mensagem de falha de comunicacao', () => {
    cepService.resposta$ = throwError(() => new CepConsultaErro('falha-conexao'));

    preencherCep('01001-000');
    botaoBuscar().click();
    fixture.detectChanges();

    expect(textoTela()).toContain('Não foi possível conectar à API');
  });

  it('limpa formulario, mensagens e devolve foco ao CEP', () => {
    preencherCep('01001-000');
    botaoBuscar().click();
    fixture.detectChanges();

    botaoLimpar().click();
    fixture.detectChanges();

    expect(component.formulario.controls.cep.value).toBe('');
    expect(component.formulario.controls.logradouro.value).toBe('');
    expect(component.enderecoEncontrado()).toBeNull();
    expect(component.mensagemSucesso()).toBe('');
    expect(document.activeElement).toBe(inputCep());
  });

  it('mostra indicador de carregamento', () => {
    const consultaPendente = new Subject<Endereco>();
    cepService.resposta$ = consultaPendente.asObservable();

    preencherCep('01001-000');
    botaoBuscar().click();
    fixture.detectChanges();

    expect(component.carregando()).toBe(true);
    expect(botaoBuscar().textContent).toContain('Buscando...');

    consultaPendente.next(enderecoPadrao);
    consultaPendente.complete();
  });

  it('previne requisicoes duplicadas enquanto carrega', () => {
    const consultaPendente = new Subject<Endereco>();
    cepService.resposta$ = consultaPendente.asObservable();

    preencherCep('01001-000');
    botaoBuscar().click();
    botaoBuscar().click();

    expect(cepService.chamadas).toEqual(['01001-000']);

    consultaPendente.next(enderecoPadrao);
    consultaPendente.complete();
  });

  function preencherCep(valor: string): void {
    const input = inputCep();
    input.value = valor;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  function inputCep(): HTMLInputElement {
    return fixture.nativeElement.querySelector('#cep') as HTMLInputElement;
  }

  function botaoBuscar(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('.botao-primario') as HTMLButtonElement;
  }

  function botaoLimpar(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('.botao-secundario') as HTMLButtonElement;
  }

  function textoTela(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }
});
