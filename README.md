# Consultar CEP

Uma aplicação para consulta de CEP com painel web em Angular e API própria em Spring Boot.

O fluxo de dados da aplicação funciona da seguinte maneira:

```txt
Frontend Angular -> API Spring Boot -> ViaCEP -> BrasilAPI como fallback
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

### Endpoint com campos selecionados

```http
GET /api/v1/ceps/{cep}/campos?campos=cep,logradouro,bairro,cidade,uf
```

Uso recomendado:
* Sistemas que precisam preencher cadastro de usuários, clientes, fornecedores etc., recebendo apenas os campos necessários.
* Redução de payload da resposta para clientes específicos.

O parâmetro `campos` é obrigatório. Aceita os mesmos campos presentes no endpoint detalhado, separados por vírgula. Se for solicitado um campo inexistente ou a lista for vazia, a API retorna erro 400.

Exemplo de requisição:
```http
GET http://localhost:8080/api/v1/ceps/01001000/campos?campos=cep,cidade,uf
```

Exemplo de resposta:
```json
{
  "cep": "01001-000",
  "cidade": "São Paulo",
  "uf": "SP"
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

## Proteção por API Key

A API possui suporte a proteção por API key, pensada para consumo por sistemas externos. Por padrão, em desenvolvimento local, a proteção pode ficar desabilitada para não interferir no painel Angular.

Para habilitar, defina as variáveis de ambiente:

Windows (cmd):
```cmd
set API_KEY_HABILITADA=true
set API_KEY_VALOR=sua-chave-aqui
```

Windows (PowerShell):
```powershell
$env:API_KEY_HABILITADA="true"
$env:API_KEY_VALOR="sua-chave-aqui"
```

Linux/macOS:
```bash
export API_KEY_HABILITADA=true
export API_KEY_VALOR=sua-chave-aqui
```

Quando habilitada, as requisições aos endpoints `/api/v1/**` passam a exigir o header `X-API-Key`.
Requisições sem o header correto retornarão `401 Unauthorized`.

Exemplo com curl:
```bash
curl -H "X-API-Key: sua-chave-aqui" http://localhost:8080/api/v1/ceps/01001000
```

> **Aviso de Segurança**: Nunca versione chaves reais no repositório. Configure sempre via variáveis de ambiente em produção. Esta é uma proteção simples e não substitui autenticação completa para usuários.

## Proteção por Rate Limit

A API possui um mecanismo simples de Rate Limit em memória, pensado para proteger os endpoints `/api/v1/**` contra excesso de requisições. 

O limite é aplicado da seguinte forma:
1. Por API Key, caso esteja habilitada e seja fornecida na requisição.
2. Por IP da requisição, caso a API Key não seja fornecida ou esteja desabilitada.

Em ambiente de desenvolvimento, o Rate Limit fica habilitado por padrão com configurações flexíveis, mas pode ser desativado.

Para configurar, defina as variáveis de ambiente:

Windows (cmd):
```cmd
set RATE_LIMIT_HABILITADO=true
set RATE_LIMIT_REQUISICOES=60
set RATE_LIMIT_JANELA=1m
```

Windows (PowerShell):
```powershell
$env:RATE_LIMIT_HABILITADO="true"
$env:RATE_LIMIT_REQUISICOES="60"
$env:RATE_LIMIT_JANELA="1m"
```

Linux/macOS:
```bash
export RATE_LIMIT_HABILITADO=true
export RATE_LIMIT_REQUISICOES=60
export RATE_LIMIT_JANELA=1m
```

Quando o limite de requisições for excedido dentro da janela de tempo, a API retornará `429 Too Many Requests`:

```json
{
  "status": 429,
  "erro": "LIMITE_REQUISICOES_EXCEDIDO",
  "mensagem": "Limite de requisições excedido. Tente novamente mais tarde.",
  "caminho": "/api/v1/ceps/01001000",
  "timestamp": "2026-09-11T12:00:00Z"
}
```

## Uso em outros sistemas

Para integrações com outros sistemas, como telas de cadastro de usuários, clientes, fornecedores ou pacientes, recomenda-se utilizar inicialmente o endpoint básico (lembrando de passar o header `X-API-Key` se a proteção estiver habilitada):

```http
GET /api/v1/ceps/{cep}
X-API-Key: sua-chave-aqui
```

Esse endpoint retorna apenas os dados principais necessários para preenchimento automático de endereço. Quando forem necessários dados complementares, utilize o endpoint detalhado:

```http
GET /api/v1/ceps/{cep}/detalhes
X-API-Key: sua-chave-aqui
```

## Limitações atuais

* A API usa cache em memória para consultas bem-sucedidas.
* O cache reduz chamadas repetidas aos provedores externos (ViaCEP e BrasilAPI).
* O cache é local à instância da aplicação.
* Em reinício da aplicação, o cache é perdido.
* Ainda não há Redis ou cache distribuído.
* Em ambiente com múltiplas instâncias, cada instância teria seu próprio cache e rate limit.
* Rate limit é em memória e não distribuído, portanto, contadores são perdidos no reinício da aplicação.
* A API utiliza o ViaCEP como provedor principal de CEPs.
* A BrasilAPI é utilizada como fallback automático caso o ViaCEP apresente falhas (indisponibilidade, timeout, resposta inválida).
* Em caso de CEP inexistente, o fallback não é acionado (retorna 404 imediato).
* A fonte dos dados pode ser identificada no campo `fonte` da resposta detalhada (`VIACEP` ou `BRASILAPI`).
* `localizacao` retorna `null` nesta fase, pois os dados de localização não são padronizados ou fornecidos pelo ViaCEP.
* O projeto ainda está preparado para execução local.

## Próximas evoluções planejadas

* seleção de campos na resposta;
* rate limit distribuído com Redis;
* métricas/observabilidade;
* gerenciamento de múltiplas API keys;
* Docker;
* deploy;
* integração com sistemas como Voll.med.
