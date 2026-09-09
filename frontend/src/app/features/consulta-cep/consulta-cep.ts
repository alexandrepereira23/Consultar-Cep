import { ChangeDetectionStrategy, Component, ElementRef, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Endereco } from '../../core/models/endereco';
import { CepConsultaErro, CepService } from '../../core/services/cep.service';

@Component({
  selector: 'app-consulta-cep',
  imports: [ReactiveFormsModule],
  templateUrl: './consulta-cep.html',
  styleUrl: './consulta-cep.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConsultaCepComponent implements OnDestroy {
  @ViewChild('cepInput') private cepInput?: ElementRef<HTMLInputElement>;

  private readonly formBuilder = inject(FormBuilder);
  private readonly cepService = inject(CepService);

  readonly carregando = signal(false);
  readonly enderecoEncontrado = signal<Endereco | null>(null);
  readonly mensagemErro = signal('');
  readonly mensagemSucesso = signal('');
  readonly cepComCaracteresInvalidos = signal(false);

  private temporizadorMensagem?: ReturnType<typeof setTimeout>;

  readonly formulario = this.formBuilder.nonNullable.group({
    cep: ['', [Validators.required, this.validarCep]],
    logradouro: [''],
    bairro: [''],
    cidade: [''],
    uf: [''],
    complemento: [''],
    unidade: [''],
  });

  buscar(): void {
    this.cepComCaracteresInvalidos.set(false);
    this.formulario.controls.cep.markAsTouched();

    if (this.formulario.invalid || this.carregando()) {
      return;
    }

    const cep = this.formulario.controls.cep.value;

    this.carregando.set(true);
    this.limparResultadoEndereco();

    this.cepService.buscar(cep).pipe(
      finalize(() => this.carregando.set(false)),
    ).subscribe({
      next: (endereco) => {
        this.enderecoEncontrado.set(endereco);
        this.exibirSucesso(`CEP ${endereco.cep} encontrado. Os campos foram preenchidos e podem ser editados.`);
        this.formulario.patchValue({
          logradouro: endereco.logradouro,
          bairro: endereco.bairro,
          cidade: endereco.cidade,
          uf: endereco.uf,
          complemento: endereco.complemento,
          unidade: endereco.unidade,
        });
      },
      error: (erro: unknown) => {
        this.exibirErro(this.mensagemParaErro(erro));
      },
    });
  }

  limpar(): void {
    this.formulario.reset();
    this.cepComCaracteresInvalidos.set(false);
    this.limparResultadoEndereco();
    this.cepInput?.nativeElement.focus();
  }

  fecharNotificacao(): void {
    this.limparTemporizadorMensagem();
    this.mensagemErro.set('');
    this.mensagemSucesso.set('');
  }

  ngOnDestroy(): void {
    this.limparTemporizadorMensagem();
  }

  aoDigitarCep(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const valorOriginal = input.value;
    const somenteNumeros = valorOriginal.replace(/\D/g, '').slice(0, 8);
    const valorMascarado = this.aplicarMascaraCep(somenteNumeros);

    this.cepComCaracteresInvalidos.set(/[^\d.\-\s]/.test(valorOriginal));
    this.formulario.controls.cep.setValue(valorMascarado, { emitEvent: false });
    input.value = valorMascarado;
  }

  campoCepInvalido(): boolean {
    const cep = this.formulario.controls.cep;
    return cep.invalid && (cep.dirty || cep.touched);
  }

  podeBuscar(): boolean {
    return this.formulario.controls.cep.valid && !this.carregando();
  }

  mensagemValidacaoCep(): string {
    const cep = this.formulario.controls.cep;

    if (this.cepComCaracteresInvalidos()) {
      return 'Use apenas numeros no CEP.';
    }

    if (!this.campoCepInvalido()) {
      return '';
    }

    if (cep.hasError('required')) {
      return 'O CEP e obrigatorio.';
    }

    if (cep.hasError('tamanhoCep')) {
      return 'O CEP deve conter exatamente oito numeros.';
    }

    if (cep.hasError('caracteresInvalidos')) {
      return 'Use apenas numeros no CEP.';
    }

    return '';
  }

  private aplicarMascaraCep(cep: string): string {
    if (cep.length <= 5) {
      return cep;
    }

    return `${cep.slice(0, 5)}-${cep.slice(5)}`;
  }

  private limparResultadoEndereco(): void {
    this.enderecoEncontrado.set(null);
    this.limparTemporizadorMensagem();
    this.mensagemErro.set('');
    this.mensagemSucesso.set('');
    this.formulario.patchValue({
      logradouro: '',
      bairro: '',
      cidade: '',
      uf: '',
      complemento: '',
      unidade: '',
    });
  }

  private exibirSucesso(mensagem: string): void {
    this.mensagemErro.set('');
    this.mensagemSucesso.set(mensagem);
    this.agendarLimpezaMensagem();
  }

  private exibirErro(mensagem: string): void {
    this.mensagemSucesso.set('');
    this.mensagemErro.set(mensagem);
    this.agendarLimpezaMensagem();
  }

  private agendarLimpezaMensagem(): void {
    this.limparTemporizadorMensagem();
    this.temporizadorMensagem = setTimeout(() => {
      this.mensagemErro.set('');
      this.mensagemSucesso.set('');
    }, 4000);
  }

  private limparTemporizadorMensagem(): void {
    if (this.temporizadorMensagem) {
      clearTimeout(this.temporizadorMensagem);
      this.temporizadorMensagem = undefined;
    }
  }

  private mensagemParaErro(erro: unknown): string {
    if (!(erro instanceof CepConsultaErro)) {
      return 'Nao foi possivel consultar o CEP agora. Tente novamente em instantes.';
    }

    const mensagens: Record<typeof erro.tipo, string> = {
      'cep-invalido': 'Informe um CEP com exatamente oito numeros.',
      'cep-nao-encontrado': 'CEP nao encontrado. Confira os oito digitos e tente novamente.',
      'falha-conexao': 'Falha de comunicacao. Verifique sua conexao e tente novamente.',
      'servico-indisponivel': 'Servico de CEP indisponivel no momento. Tente novamente mais tarde.',
    };

    return mensagens[erro.tipo];
  }

  private validarCep(control: AbstractControl<string>): ValidationErrors | null {
    const valor = control.value ?? '';

    if (!valor) {
      return null;
    }

    if (/[^\d.\-\s]/.test(valor)) {
      return { caracteresInvalidos: true };
    }

    return valor.replace(/\D/g, '').length === 8 ? null : { tamanhoCep: true };
  }
}
