# Consultar CEP

Uma aplicação para consulta de CEP com painel web em Angular e API própria em Spring Boot.

O fluxo de dados da aplicação funciona da seguinte maneira:

```txt
Painel Web (Angular / Vercel) -> Endpoints do Painel (/api/v1/painel/**) -> API Spring Boot -> ViaCEP -> BrasilAPI (fallback)
Sistemas Externos / Backends  -> Endpoints de Integração (/api/v1/ceps/**) [com X-API-Key] -> API Spring Boot -> ViaCEP -> BrasilAPI (fallback)
```

## Funcionalidades atuais

* Consulta de CEP pelo painel web (com dados básicos e completos).
* Endpoints dedicados para o painel web (`/api/v1/painel/**`) sem necessidade de API key.
* Endpoints dedicados para integração com outros sistemas (`/api/v1/ceps/**`) protegidos por API key em produção.
* Preenchimento automático e edição dos campos de endereço no frontend.
* Fallback transparente: ViaCEP como provedor principal e BrasilAPI como fallback.
* Cache em memória com Caffeine.
* Rate limit em memória configurável (por API Key ou por IP).
* Suporte a perfis de ambiente (`local` e `prod`) com configuração flexível e desacoplada de nuvem.
* CORS configurável para origens únicas ou múltiplas.
* Tratamento padronizado de erros.
* Documentação Swagger/OpenAPI (habilitada em desenvolvimento).

## Arquitetura do projeto

O projeto está estruturado em monorepo com as seguintes partes:

```txt
frontend/   Aplicação Angular
backend/    API Spring Boot
```

### Separação de acessos

1. **Painel Web (Angular):**
   * Destinado ao uso humano via navegador/celular.
   * Consome os endpoints próprios do painel:
     * `GET /api/v1/painel/ceps/{cep}`
     * `GET /api/v1/painel/ceps/{cep}/detalhes`
   * **Não utiliza `X-API-Key`**, garantindo que nenhuma chave secreta fique exposta no bundle público do frontend.
   * Protegido por Rate Limit (por IP) e CORS restrito ao domínio do frontend.

2. **API de Integração (Sistemas externos):**
   * Destinada a integrações de backend-para-backend (ex.: Voll.med, microsserviços, automações).
   * Consome os endpoints de integração:
     * `GET /api/v1/ceps/{cep}`
     * `GET /api/v1/ceps/{cep}/detalhes`
     * `GET /api/v1/ceps/{cep}/campos?campos=cep,cidade,uf`
   * Em produção, exige o envio do header `X-API-Key`.
   * Protegida por Rate Limit (por API key ou IP).

## Tecnologias utilizadas

**Frontend:**
* Angular 21
* TypeScript
* RxJS
* Vitest

**Backend:**
* Java 21+
* Spring Boot 4
* Spring Web MVC
* Springdoc / OpenAPI
* Caffeine Cache
* Bucket4j (Rate Limiting)
* Maven

---

## Ambientes e Configuração

O projeto foi projetado utilizando variáveis de ambiente genéricas, permitindo deploy inicial no **Railway** (backend) e na **Vercel** (frontend), além de suportar migração futura para infraestrutura própria (Docker, VM, Kubernetes) sem acoplamento a provedores específicos.

### Arquivo `.env.example`

Na raiz do projeto encontra-se o arquivo `.env.example` com o modelo de variáveis de ambiente:

```env
# Backend - Spring Boot
SPRING_PROFILES_ACTIVE=prod
PORT=8080

API_KEY_VALOR=troque-por-uma-chave-forte
RATE_LIMIT_REQUISICOES=60
RATE_LIMIT_JANELA=1m
CORS_ORIGENS_PERMITIDAS=https://seu-frontend.vercel.app

VIACEP_URL_BASE=https://viacep.com.br/ws
BRASILAPI_URL_BASE=https://brasilapi.com.br/api/cep/v2

# Frontend - Angular/Vercel
API_BASE_URL=https://seu-backend.railway.app
```

> **Atenção de Segurança**: Nunca comite chaves ou segredos reais no repositório. Nunca coloque `API_KEY_VALOR` no código ou variáveis públicas do frontend Angular.

### Perfis de Ambiente do Backend

* `application.yml`: Configurações comuns (porta `${PORT:8080}`, timeouts, integrações e cache). O perfil padrão é definido como `local` quando nenhum perfil é explicitamente informado.
* `application-local.yml`: Perfil voltado ao desenvolvimento local:
  * API Key desabilitada (`aplicacao.api-key.habilitada=false`).
  * Rate limit relaxado (120 req/min).
  * CORS liberado para `http://localhost:4200`.
  * Swagger/OpenAPI habilitado.
* `application-prod.yml`: Perfil voltado para produção (Railway ou servidor próprio):
  * API Key habilitada obrigatoriamente (`aplicacao.api-key.habilitada=true`, `valor: ${API_KEY_VALOR}`).
  * CORS obrigatório (`aplicacao.cors.origens-permitidas: ${CORS_ORIGENS_PERMITIDAS}`). Suporta uma origem ou múltiplas separadas por vírgula (ex.: `https://meu-app.vercel.app,https://outro-dominio.com`).
  * Rate limit padrão de 60 req/min configurável por `RATE_LIMIT_REQUISICOES` e `RATE_LIMIT_JANELA`.
  * Swagger UI e `/v3/api-docs` desabilitados para proteção de endpoints em produção.

---

## Como executar o projeto

### Backend

#### Pré-requisitos
* JDK 21+ instalado.
* Maven Wrapper incluso no repositório.

#### Executando localmente (Perfil `local`)

Windows:
```cmd
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Linux/macOS:
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Por padrão, a API estará acessível em:
```txt
http://localhost:8080
```

#### Executando em Produção (Railway ou Servidor Próprio)

Defina as variáveis no painel da sua plataforma de hospedagem:

* `SPRING_PROFILES_ACTIVE`: `prod`
* `PORT`: atribuído automaticamente pelo Railway ou definido manualmente (ex.: `8080`).
* `API_KEY_VALOR`: chave secreta forte para proteger endpoints de integração.
* `CORS_ORIGENS_PERMITIDAS`: URL do frontend (ex.: `https://seu-frontend.vercel.app` ou múltiplas separadas por vírgula).
* `RATE_LIMIT_REQUISICOES`: limite de requisições (ex.: `60`).
* `RATE_LIMIT_JANELA`: janela de tempo do rate limit (ex.: `1m`).
* `VIACEP_URL_BASE` (opcional): padrão `https://viacep.com.br/ws`.
* `BRASILAPI_URL_BASE` (opcional): padrão `https://brasilapi.com.br/api/cep/v2`.

---

### Frontend

#### Pré-requisitos
* Node.js compatível com Angular 21.
* npm.

#### Executando localmente

```bash
cd frontend
npm install
npm start
```

O frontend estará disponível em:
```txt
http://localhost:4200
```

#### Configuração da URL da API no Frontend

O frontend consome a API através da propriedade `apiBaseUrl` definida em:
```txt
frontend/src/environments/environment.ts
```

* Em desenvolvimento local aponta para: `http://localhost:8080/api/v1`
* Para publicação na Vercel ou outro servidor web, atualize o valor de `apiBaseUrl` com o domínio da sua API (ex.: `https://seu-backend.railway.app/api/v1`).

---

## Endpoints da API

### Endpoints do Painel Web (Sem API Key)

Dedicados para consumo da interface web / mobile, protegidos por rate limit e CORS restrito:

#### 1. Consulta básica do painel
```http
GET /api/v1/painel/ceps/{cep}
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

#### 2. Consulta detalhada do painel
```http
GET /api/v1/painel/ceps/{cep}/detalhes
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

---

### Endpoints de Integração (Protegidos por API Key em produção)

Dedicados para integração com outros sistemas de backend. Em ambiente de produção, exigem o cabeçalho `X-API-Key`:

#### 1. Consulta básica
```http
GET /api/v1/ceps/{cep}
X-API-Key: sua-chave-aqui
```

#### 2. Consulta detalhada
```http
GET /api/v1/ceps/{cep}/detalhes
X-API-Key: sua-chave-aqui
```

#### 3. Consulta com campos selecionados
```http
GET /api/v1/ceps/{cep}/campos?campos=cep,cidade,uf
X-API-Key: sua-chave-aqui
```
Exemplo de resposta:
```json
{
  "cep": "01001-000",
  "cidade": "São Paulo",
  "uf": "SP"
}
```

---

## Formatos de CEP e Erros Padronizados

Formatos aceitos: `00000000` e `00000-000`.

Estrutura de resposta de erro:
```json
{
  "status": 400,
  "erro": "CEP_INVALIDO",
  "mensagem": "O CEP informado é inválido. Use o formato 00000000 ou 00000-000.",
  "caminho": "/api/v1/painel/ceps/123",
  "timestamp": "2026-09-14T12:00:00Z"
}
```

| Status | Código | Motivo |
|---|---|---|
| 400 | `CEP_INVALIDO` | CEP com formato incompatível |
| 400 | `CAMPOS_INVALIDOS` | Parâmetro de campos ausente ou contém campo inválido |
| 401 | `API_KEY_INVALIDA` | API Key ausente ou incorreta em endpoint protegido |
| 404 | `CEP_NAO_ENCONTRADO` | CEP inexistente |
| 429 | `LIMITE_REQUISICOES_EXCEDIDO` | Rate limit excedido para a janela de tempo |
| 500 | `ERRO_INTERNO` | Falha interna no servidor |
| 502 | `RESPOSTA_FORNECEDOR_INVALIDA` | Fornecedor externo retornou resposta malformada |
| 503 | `SERVICO_CEP_INDISPONIVEL` | Provedores externos temporariamente indisponíveis |

---

## Como executar os testes

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

### Frontend
Windows:
```cmd
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

Linux/macOS:
```bash
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

---

## Limitações atuais e próximas etapas

* Cache em memória por instância (Caffeine). Em ambiente com múltiplas instâncias, o cache é local a cada réplica.
* Rate limit em memória gerenciado localmente via Bucket4j.
* Próximas evoluções planejadas:
  * Cache e Rate Limit distribuídos com Redis.
  * Autenticação OAuth2 / JWT para múltiplos usuários / clientes.
  * Containerização com Docker e Docker Compose.
