# Consulta de CEP

Monorepo que contém o painel Angular de consulta de CEP (Fase 1) e uma API Spring Boot (Fase 2).

Autor: Alexandre Henrique Pereira Pires

## Estado Atual

- `frontend/`: aplicação Angular para consulta de endereço por CEP brasileiro.
- `backend/`: API Spring Boot que atua como gateway para consultas ao ViaCEP, adicionando validações e normalização.
- O backend está implementado e funcional de maneira independente.
- O frontend ainda consulta diretamente o ViaCEP.
- A integração do frontend com o backend será realizada na Fase 3.

## Funcionalidades do Frontend

- Consulta de endereço por CEP com ou sem máscara.
- Máscara visual no formato `00000-000`.
- Validação de CEP obrigatório com exatamente oito números.
- Rejeição de letras e caracteres inválidos.
- Busca pelo botão `Buscar` ou pela tecla Enter.
- Indicação de carregamento durante a consulta.
- Preenchimento automático dos campos de endereço.
- Edição manual dos campos após a consulta.
- Botão `Limpar` para restaurar o estado inicial e devolver o foco ao CEP.
- Mensagens claras para CEP inválido, CEP inexistente e falhas de comunicação.
- Layout responsivo com foco visível e HTML semântico.

## Funcionalidades do Backend (API)

- Endpoint REST básico: `GET /api/v1/ceps/{cep}`.
- Endpoint REST detalhado: `GET /api/v1/ceps/{cep}/detalhes`.
- Aceita CEP sem máscara (`01001000`) ou com máscara (`01001-000`).
- Validação estrita dos formatos aceitos, retornando `400 Bad Request` para entradas inválidas.
- Comunicação com a API externa do ViaCEP usando `RestClient`.
- Padronização da resposta em formato JSON.
- Retorno 404 Not Found caso o ViaCEP não encontre o CEP.
- Tratamento de exceções global com mensagens padronizadas.
- Integração com `springdoc-openapi` para interface Swagger UI.
- Configuração de CORS para permitir `GET` e `OPTIONS` a partir das origens configuradas.

## Contrato dos Endpoints

### `GET /api/v1/ceps/{cep}`

Resposta básica:

```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "bairro": "Sé",
  "cidade": "São Paulo",
  "uf": "SP"
}
```

### `GET /api/v1/ceps/{cep}/detalhes`

Resposta detalhada:

```json
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
```

`localizacao` é tipada como:

```json
{
  "latitude": -23.55052,
  "longitude": -46.633308
}
```

Nesta fase, o ViaCEP não fornece latitude e longitude. Por isso, o campo `localizacao` permanece `null`.

## Campos Retornados

Campos principais:

- CEP
- Endereço (logradouro)
- Bairro
- Cidade (Localidade)
- UF

Informações detalhadas adicionais (para uso futuro ou interfaces estendidas):

- Estado
- Região
- Código IBGE
- DDD
- SIAFI
- GIA
- Complemento
- Unidade
- Localização tipada, atualmente nula
- Fonte interna usada para montar os dados

## Tecnologias Utilizadas

**Frontend:**
- Angular 21.
- TypeScript com configuração estrita.
- Componentes standalone.
- Reactive Forms.
- Vitest via Angular CLI.
- CSS sem biblioteca visual externa.

**Backend:**
- Java 21
- Spring Boot 4.1.1
- Spring Web (REST Controllers e RestClient)
- JUnit 5 e Mockito
- Maven
- Springdoc OpenAPI 3.1.1 (Swagger)

## Pré-requisitos

- Node.js (compatível com Angular 21) e npm.
- Java 21 JDK (Para rodar e compilar o backend).
- Maven 3.9+ (O projeto inclui o Maven Wrapper `mvnw`).

## Execução Local (Backend)

O backend roda por padrão na porta `8080`.

### Linux e macOS

```bash
cd backend
./mvnw spring-boot:run
./mvnw test
./mvnw clean package
```

### Windows CMD ou PowerShell

```cmd
cd backend
mvnw.cmd spring-boot:run
mvnw.cmd test
mvnw.cmd clean package
```

Acesse a documentação da API via Swagger UI em:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`

O endereço `/swagger-ui.html` redireciona para `/swagger-ui/index.html`. A especificação OpenAPI fica disponível em `http://localhost:8080/v3/api-docs`.

## Execução Local (Frontend)

O frontend roda por padrão na porta `4200`.

```bash
cd frontend
npm install
npm start
```

A aplicação fica disponível em `http://localhost:4200/`.

## Testes

**Frontend:**
```bash
cd frontend
npm test -- --watch=false
```

**Backend:**

Linux e macOS:

```bash
cd backend
./mvnw test
```

Windows CMD ou PowerShell:

```cmd
cd backend
mvnw.cmd test
```

## Serviço Externo de CEP

Atualmente o backend funciona como um gateway para o **ViaCEP**, e realiza as requisições HTTPS para `https://viacep.com.br/ws/{cep}/json/`. A URL e os timeouts são configurados via `application.yml`.

## Tratamento de Erros

O backend padroniza os erros nos seguintes cenários:

- `400 Bad Request`, `CEP_INVALIDO`: entrada fora dos formatos `00000000` ou `00000-000`.
- `404 Not Found`, `CEP_NAO_ENCONTRADO`: CEP inexistente.
- `502 Bad Gateway`, `RESPOSTA_FORNECEDOR_INVALIDA`: resposta nula, malformada, incompatível ou sem campos essenciais.
- `503 Service Unavailable`, `SERVICO_CEP_INDISPONIVEL`: timeout, falha de DNS, conexão recusada ou erro HTTP 5xx do fornecedor.
- `500 Internal Server Error`, `ERRO_INTERNO`: erro inesperado.

Formato do erro:

```json
{
  "status": 404,
  "erro": "CEP_NAO_ENCONTRADO",
  "mensagem": "O CEP informado não foi encontrado.",
  "caminho": "/api/v1/ceps/99999999",
  "timestamp": "2026-09-09T12:00:00Z"
}
```

As respostas públicas não expõem URL externa, stack trace, nomes de classes, mensagens internas da biblioteca HTTP nem detalhes de conexão.

## CORS

As origens permitidas são configuráveis por `application.yml` e variáveis de ambiente:

```yaml
aplicacao:
  cors:
    origens-permitidas: http://localhost:4200
```

Métodos permitidos pelo CORS:

- `GET`
- `OPTIONS`

Não é utilizado `*` como origem permitida.

## Situação Atual do Frontend

O frontend Angular permanece inalterado nesta fase e ainda consulta diretamente o ViaCEP. A integração com o backend será feita na Fase 3.

## Próximos Passos

- Configurar o frontend para chamar o backend criado (`http://localhost:8080/api/v1/ceps`) ao invés de bater direto no ViaCEP.
- Adicionar logs, métricas e rastreabilidade.

## Licença

Este projeto está licenciado sob a licença MIT. Consulte `LICENSE` para mais detalhes.
