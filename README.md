# Consultar CEP

Uma aplicação para consulta de CEP com painel web em Angular e API própria em Spring Boot.

O fluxo de dados da aplicação funciona da seguinte maneira:

```txt
Frontend Angular -> API Spring Boot -> ViaCEP
```

## Funcionalidades atuais

* Consulta de CEP pelo painel web.
* Preenchimento dos dados principais do endereço.
* Exibição de informações detalhadas.
* API REST própria para consulta de CEP.
* Endpoint básico para consumo por outros sistemas.
* Endpoint detalhado para painel e consultas completas.
* Tratamento padronizado de erros.
* Documentação Swagger/OpenAPI.

## Arquitetura do projeto

O projeto está estruturado em um formato monorepo com as seguintes partes:

```txt
frontend/   Aplicação Angular
backend/    API Spring Boot
```

Responsabilidades:
* Angular: interface visual.
* Spring Boot: API intermediária, validação, padronização da resposta e comunicação com provedor externo.
* ViaCEP: provedor externo usado pela API, não diretamente pelo frontend.

## Tecnologias utilizadas

**Frontend:**
* Angular
* TypeScript
* RxJS
* Vitest

**Backend:**
* Java
* Spring Boot
* Spring Web
* Springdoc/OpenAPI
* Maven

## Como executar o backend

### Pré-requisitos
* JDK compatível com o projeto.
* Maven Wrapper incluso no projeto.

### Windows
```cmd
cd backend
mvnw.cmd spring-boot:run
```

### Linux/macOS
```bash
cd backend
./mvnw spring-boot:run
```

Por padrão, a API roda em:
```txt
http://localhost:8080
```

## Como executar o frontend

### Pré-requisitos
* Node.js compatível com Angular 21.
* npm.

### Comandos
```cmd
cd frontend
npm ci
npm start
```

Por padrão, o Angular roda em:
```txt
http://localhost:4200
```

O frontend espera a API em:
```txt
http://localhost:8080/api/v1
```

## Configuração da URL da API no frontend

A configuração da URL da API fica localizada em:
```txt
frontend/src/environments/environment.ts
```

Em ambiente local, o valor atual aponta para:
```txt
http://localhost:8080/api/v1
```

## Como testar

### Frontend

Windows:
```cmd
cd frontend
npm ci
set NG_CLI_ANALYTICS=false
set CI=true
npm test -- --watch=false
npm run build
```

Linux/macOS:
```bash
cd frontend
npm ci
NG_CLI_ANALYTICS=false CI=true npm test -- --watch=false
NG_CLI_ANALYTICS=false CI=true npm run build
```

### Backend

Windows:
```cmd
cd backend
mvnw.cmd test
```

Linux/macOS:
```bash
cd backend
./mvnw test
```

## Documentação Swagger/OpenAPI

Quando o backend estiver rodando, acesse:
```txt
http://localhost:8080/swagger-ui.html
```

## Endpoints da API

### Endpoint básico

```http
GET /api/v1/ceps/{cep}
```

Uso recomendado:
* Sistemas externos que precisam apenas dos dados principais do endereço.
* Exemplos: cadastro de usuários, clientes, fornecedores, pacientes etc.

Exemplo de requisição:
```http
GET http://localhost:8080/api/v1/ceps/01001000
```

Exemplo de resposta:
```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "bairro": "Sé",
  "cidade": "São Paulo",
  "uf": "SP"
}
```

### Endpoint detalhado

```http
GET /api/v1/ceps/{cep}/detalhes
```

Uso recomendado:
* Painel Angular.
* Consultas que precisam de dados complementares.

Exemplo de requisição:
```http
GET http://localhost:8080/api/v1/ceps/01001000/detalhes
```

Exemplo de resposta:
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

## Formatos de CEP aceitos

* `01001000`
* `01001-000`

A API valida o CEP e retorna erro padronizado quando o formato é inválido.

## Respostas de erro

Formato padrão:
```json
{
  "status": 404,
  "erro": "CEP_NAO_ENCONTRADO",
  "mensagem": "O CEP informado não foi encontrado.",
  "caminho": "/api/v1/ceps/99999999",
  "timestamp": "2026-09-09T12:00:00Z"
}
```

Principais erros:

| Status | Código | Quando ocorre |
| --- | --- | --- |
| 400 | `CEP_INVALIDO` | CEP em formato inválido |
| 404 | `CEP_NAO_ENCONTRADO` | CEP não encontrado |
| 500 | `ERRO_INTERNO` | Erro interno inesperado |
| 502 | `RESPOSTA_FORNECEDOR_INVALIDA` | Provedor externo retornou resposta inválida |
| 503 | `SERVICO_CEP_INDISPONIVEL` | Serviço externo temporariamente indisponível |

## Uso em outros sistemas

Para integrações com outros sistemas, como telas de cadastro de usuários, clientes, fornecedores ou pacientes, recomenda-se utilizar inicialmente o endpoint básico:

```http
GET /api/v1/ceps/{cep}
```

Esse endpoint retorna apenas os dados principais necessários para preenchimento automático de endereço. Quando forem necessários dados complementares, utilize o endpoint detalhado:

```http
GET /api/v1/ceps/{cep}/detalhes
```

## Limitações atuais

* A API ainda não possui autenticação.
* A API usa cache em memória para consultas bem-sucedidas.
* O cache reduz chamadas repetidas ao ViaCEP.
* O cache é local à instância da aplicação.
* Em reinício da aplicação, o cache é perdido.
* Ainda não há Redis ou cache distribuído.
* Em ambiente com múltiplas instâncias, cada instância teria seu próprio cache.
* A API ainda não possui rate limit.
* A API ainda depende do ViaCEP como provedor externo.
* `localizacao` retorna `null` nesta fase, porque o ViaCEP não fornece latitude e longitude.
* O projeto ainda está preparado para execução local.

## Próximas evoluções planejadas

* seleção de campos na resposta;
* autenticação por API key para consumo externo;
* rate limit;
* fallback com outro provedor, como BrasilAPI;
* Docker;
* deploy;
* integração com sistemas como Voll.med.
