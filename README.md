# Consulta de CEP

Monorepo preparado para manter o painel Angular de consulta de CEP e receber futuramente uma API Spring Boot.

Autor: Alexandre Henrique Pereira Pires

## Estado Atual

- `frontend/`: aplicacao Angular para consulta de endereco por CEP brasileiro usando o servico publico ViaCEP.
- `backend/`: ainda nao implementado. A pasta sera criada em uma etapa futura.

## Funcionalidades do Frontend

- Consulta de endereco por CEP com ou sem mascara.
- Mascara visual no formato `00000-000`.
- Validacao de CEP obrigatorio com exatamente oito numeros.
- Rejeicao de letras e caracteres invalidos.
- Busca pelo botao `Buscar` ou pela tecla Enter.
- Indicacao de carregamento durante a consulta.
- Preenchimento automatico dos campos de endereco.
- Edicao manual dos campos apos a consulta.
- Botao `Limpar` para restaurar o estado inicial e devolver o foco ao CEP.
- Mensagens claras para CEP invalido, CEP inexistente e falhas de comunicacao.
- Layout responsivo com foco visivel e HTML semantico.

## Campos Retornados

Campos principais:

- CEP.
- Endereco ou logradouro.
- Bairro.
- Cidade.
- UF.

Informacoes detalhadas:

- Estado.
- Regiao.
- Codigo IBGE.
- DDD.
- SIAFI.
- GIA.
- Complemento.
- Unidade.

## Tecnologias Utilizadas

- Angular 21.
- TypeScript com configuracao estrita.
- Componentes standalone.
- Reactive Forms.
- HttpClient.
- RxJS.
- Vitest via Angular CLI.
- CSS sem biblioteca visual externa.

## Pre-requisitos

- Node.js compativel com Angular 21.
- npm compativel com o campo `packageManager` em `frontend/package.json`.

Versoes utilizadas no desenvolvimento local:

- Node.js `24.11.1`.
- npm `11.6.2`.
- Angular CLI `21.0.3`.

## Instalacao

```bash
cd frontend
npm install
```

Para uma instalacao limpa baseada no lockfile:

```bash
cd frontend
npm ci
```

## Execucao Local

```bash
cd frontend
npm start
```

A aplicacao fica disponivel em `http://localhost:4200/`.

## Testes

```bash
cd frontend
npm test -- --watch=false
```

## Build de Producao

```bash
cd frontend
npm run build
```

O build e gerado em `frontend/dist/consulta-cep/`.

## Estrutura Principal

```text
Consultar-Cep/
├── frontend/       # Painel Angular
├── backend/        # API Spring Boot - implementacao futura
├── README.md
├── LICENSE
└── .gitignore
```

Estrutura atual do frontend:

```text
frontend/
  angular.json
  package.json
  package-lock.json
  public/
    favicon.ico
  src/
    index.html
    main.ts
    styles.css
    app/
      app.config.ts
      app.html
      app.ts
      core/
        models/
          endereco.ts
          endereco-via-cep.ts
        services/
          cep.service.ts
          cep.service.spec.ts
      features/
        consulta-cep/
          consulta-cep.css
          consulta-cep.html
          consulta-cep.spec.ts
          consulta-cep.ts
```

## Servico Externo de CEP

A aplicacao consulta o ViaCEP por HTTPS:

```text
https://viacep.com.br/ws/{cep}/json/
```

A URL do fornecedor externo fica centralizada no servico Angular `CepService`.

## Tratamento de Erros

O servico e a tela diferenciam os principais estados da consulta:

- CEP invalido: impede envio e orienta o preenchimento correto.
- CEP inexistente: informa que o CEP nao foi encontrado.
- Falha de comunicacao: informa possivel problema de conexao.
- Servico indisponivel: informa indisponibilidade temporaria do fornecedor.

Detalhes tecnicos de erro nao sao exibidos para o usuario.

## Proximos Passos

- Criar uma API reutilizavel em Spring Boot.
- Disponibilizar o endpoint `GET /api/v1/enderecos/cep/{cep}`.
- Padronizar a resposta independentemente do fornecedor externo.
- Usar ViaCEP como fornecedor principal.
- Avaliar BrasilAPI como fallback ou fonte complementar.
- Adicionar timeout nas consultas externas.
- Implementar cache por CEP.
- Implementar rate limiting.
- Adicionar logs, metricas e rastreabilidade.
- Documentar a API com OpenAPI/Swagger.
- Configurar CORS para consumidores autorizados.
- Adicionar testes unitarios e de integracao para o backend.
- Preparar empacotamento com Docker.

## Licenca

Este projeto esta licenciado sob a licenca MIT. Consulte `LICENSE` para mais detalhes.
