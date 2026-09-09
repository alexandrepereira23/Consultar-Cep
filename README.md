# Consulta de CEP

Monorepo que contém o painel Angular de consulta de CEP (Fase 1) e uma API Spring Boot (Fase 2).

Autor: Alexandre Henrique Pereira Pires

## Estado Atual

- `frontend/`: aplicação Angular para consulta de endereço por CEP brasileiro.
- `backend/`: API Spring Boot que atua como gateway para consultas ao ViaCEP, adicionando validações e normalização.

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

- Endpoint REST para consulta de endereço: `GET /api/v1/ceps/{cep}`.
- Normalização de entrada (remove formatações não-numéricas).
- Validação estrita de 8 dígitos para o CEP (Retorna 400 Bad Request).
- Comunicação com a API externa do ViaCEP usando `RestClient`.
- Padronização da resposta em formato JSON para o Frontend.
- Retorno 404 Not Found caso o ViaCEP não encontre o CEP.
- Tratamento de exceções global com mensagens padronizadas.
- Integração com `springdoc-openapi` para interface Swagger UI.
- Configuração de CORS para permitir requisições do Frontend Angular.

## Campos Retornados (DTO)

Campos principais:

- CEP
- Endereco (Logradouro)
- Bairro
- Cidade (Localidade)
- UF

Informações detalhadas adicionais (para uso futuro ou interfaces estendidas):

- Estado
- Regiao
- Codigo IBGE
- DDD
- SIAFI
- GIA
- Complemento
- Unidade
- Provedor (Indica de qual API externa o CEP foi consultado)

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
- Springdoc OpenAPI (Swagger)

## Pré-requisitos

- Node.js (compatível com Angular 21) e npm.
- Java 21 JDK (Para rodar e compilar o backend).
- Maven 3.9+ (O projeto inclui o Maven Wrapper `mvnw`).

## Execução Local (Backend)

O backend roda por padrão na porta `8080`.

```bash
cd backend
./mvnw.cmd spring-boot:run
```

Acesse a documentação da API via Swagger UI em:
`http://localhost:8080/swagger-ui.html`

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
```bash
cd backend
./mvnw.cmd test
```

## Serviço Externo de CEP

Atualmente o Backend funciona como um gateway para o **ViaCEP**, e realiza as requisições HTTPS para `https://viacep.com.br/ws/{cep}/json/`. A URL e os timeouts são configurados via `application.yml`.

## Tratamento de Erros

O backend padroniza os erros nos seguintes cenários:
- **CEP Inválido (Letras, tamanho diferente de 8):** Retorna `400 Bad Request`.
- **CEP Inexistente:** Retorna `404 Not Found` se o provedor retornar `{"erro": true}`.
- **Falha no Provedor:** Retorna `503 Service Unavailable` caso haja erro de comunicação ou o ViaCEP retorne erro interno (500).

O frontend captura esses status HTTP e renderiza as mensagens correspondentes para o usuário.

## Próximos Passos (Fase 3 - Opcional)

- Configurar o frontend para chamar o backend criado (`http://localhost:8080/api/v1/ceps`) ao invés de bater direto no ViaCEP.
- Avaliar BrasilAPI como fallback ou fonte complementar no backend.
- Implementar cache por CEP (ex: Redis).
- Implementar rate limiting.
- Adicionar logs, métricas e rastreabilidade.
- Preparar empacotamento com Docker.

## Licença

Este projeto está licenciado sob a licença MIT. Consulte `LICENSE` para mais detalhes.
