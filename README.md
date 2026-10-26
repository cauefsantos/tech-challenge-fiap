# Tech Challenge FIAP

Projeto de backend para gestão de oficina mecânica, desenvolvido em Java 21 com Spring Boot 4.

## Objetivo

Centralizar atendimento, clientes, veículos, serviços, materiais, ordens de serviço e orçamentos em uma API REST para apoiar a operação da oficina.

## Status atual

O projeto já contempla a base implementada e os avanços principais do fluxo de Ordem de Serviço:

- persistência do agregado de Ordem de Serviço;
- regras de domínio e transições de status da OS;
- diagnóstico e itens de serviço/material;
- geração, disponibilidade, aprovação e rejeição de orçamento;
- validação de estoque na aprovação;
- consulta pública da OS por número e documento;
- métrica de tempo médio de execução;
- Docker e PostgreSQL como base de execução local/produtiva.

## Arquitetura

A aplicação segue estrutura monolítica em camadas:

- domain: regras e agregados;
- application: casos de uso;
- infrastructure: persistência, segurança e integrações;
- presentation: controllers REST e DTOs.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA + Hibernate
- Flyway
- PostgreSQL
- H2 para desenvolvimento e testes
- Docker e Docker Compose
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper

## Segurança

A autenticação usa usuários persistidos e senhas BCrypt. O perfil de acesso é carregado no JWT como `role` (`ADMIN`, `FUNCIONARIO` ou `CLIENTE`); endpoints não públicos usam regras explícitas por perfil.

Além disso, são adotadas as seguintes práticas:

- Autenticação e autorização via JWT.
- Armazenamento seguro de senhas utilizando BCrypt.
- Validação de CPF/CNPJ e placa de veículo.
- Uso de JPA para acesso ao banco de dados, evitando consultas SQL concatenadas.
- Externalização de configurações sensíveis por meio de variáveis de ambiente.
- Testes automatizados para validação dos fluxos críticos da aplicação.

### Análise de Vulnerabilidades

Foram realizadas análises de segurança utilizando ferramentas automatizadas para identificação de vulnerabilidades conhecidas em dependências e componentes da aplicação:

- GitHub Dependabot
- OWASP Dependency Check
- Trivy (análise da imagem Docker)

Os relatórios gerados e as evidências das análises encontram-se disponíveis na documentação do projeto e/ou nos artefatos do pipeline de integração contínua.

## Estrutura do projeto

```text
.
├── .dev/
│   ├── plans/
│   └── ...
├── docs/
│   ├── README.md
│   ├── decisoes-fase1.md
│   └── ...
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── .env.example
├── .gitignore
├── compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── .mvn/
```

## Pré-requisitos

- Java 21
- Maven Wrapper
- Docker opcional
- PostgreSQL local ou H2 para testes/local

## Configuração de ambiente

Crie o arquivo `.env` com base no exemplo:

```bash
cp .env.example .env
```

Carregue as variáveis do `.env` no terminal antes de iniciar a aplicação. Os scripts ficam na raiz do repositório e configuram somente o processo/terminal atual:

```powershell
# PowerShell: dot-source para manter as variáveis nesta sessão
. .\set-env.ps1
.\mvnw.cmd spring-boot:run
```

Se a política local bloquear scripts, habilite a execução somente na sessão atual antes de carregar:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
. .\set-env.ps1
```

```bat
rem Prompt de Comando: use CALL e execute a aplicação na mesma janela
call set-env.bat
mvnw.cmd spring-boot:run
```

```bash
# Bash: carregue o script no shell atual
source ./set-env.sh
./mvnw spring-boot:run
```

Os scripts não imprimem os valores carregados. Mantenha `.env` fora do controle de versão; ele já está listado no `.gitignore`.

Variáveis esperadas:

- `JWT_SECRET` (Base64 que decodifique para pelo menos 32 bytes; gere um valor exclusivo, por exemplo com `openssl rand -base64 32`)
- `ADMIN_USERNAME` e `ADMIN_PASSWORD` (credenciais explícitas para bootstrap; senha com pelo menos 12 caracteres)
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`
- `SPRING_PROFILES_ACTIVE`
- `JWT_TOKEN_VALIDITY`
- `DB_POOL_MIN_IDLE`, `DB_POOL_MAX_SIZE`, `DB_CONNECTION_TIMEOUT_MS`, `DB_VALIDATION_TIMEOUT_MS`, `DB_IDLE_TIMEOUT_MS` e `DB_MAX_LIFETIME_MS` (pool PostgreSQL)
- `FLYWAY_CONNECT_RETRIES`
- `OPENAPI_ENABLED` e `SWAGGER_UI_ENABLED`

Importante: não versionar segredos reais.

## Execução local

```bash
./mvnw test
./mvnw spring-boot:run
```

Aplicação disponível em:

- http://localhost:8080
- http://localhost:8080/swagger-ui.html

## Execução com Docker

```bash
docker compose up --build
```

## Banco de dados

- PostgreSQL: ambiente próximo ao de produção.
- H2: ambiente de desenvolvimento e testes locais.

## Documentação de negócios e planejamento

Documentos relevantes:

- `.dev/plans/00-plano-fase1-original-extraido.md`
- `.dev/plans/01-analise-miro-fase01.md`
- `.dev/plans/02-plano-fase1-revisado.md`
- `docs/decisoes-fase1.md`

### Decisões adotadas no MVP

- aprovação do orçamento autenticada pelo sistema;
- estoque validado e consumido na aprovação;
- `CANCELADA` ao rejeitar o orçamento;
- itens de serviço e material somente em diagnóstico;
- diagnóstico como atributo da ordem de serviço;
- material mantido como `Material`.

## Autenticação

A API usa JWT emitido por `POST /api/v1/auth/login`. O administrador inicial é criado uma única vez no banco, apenas se o username não existir; reinícios nunca redefinem sua senha. A aplicação falha ao iniciar sem segredo JWT e credenciais de bootstrap explícitos. Não há usuários ou segredos padrão.

### Perfis e usuários

| Perfil | Acesso |
|---|---|
| `ADMIN` | Gestão de usuários e todos os endpoints autenticados de clientes, veículos, serviços, materiais e ordens de serviço. |
| `FUNCIONARIO` | Endpoints operacionais de clientes, veículos, serviços, materiais e ordens de serviço; não pode gerir usuários. |
| `CLIENTE` | Sem endpoints autenticados de recursos nesta fase. Consulta pública existente de OS continua disponível por número/documento; consulta autenticada de OS por cliente está fora do escopo. |

`POST`, `GET`, `PUT` e `DELETE /api/v1/usuarios` (incluindo busca por id) são exclusivos de `ADMIN`. O `DELETE /api/v1/usuarios/{id}` desativa a conta e a atualização administrativa pode reativá-la. A criação recebe senha em texto apenas na requisição; respostas nunca contêm senha ou hash. Uma conta `CLIENTE` exige o campo `documentoCliente` com o CPF/CNPJ de um cliente cadastrado; os demais perfis não aceitam vínculo. O username é normalizado para minúsculas e único sem distinção de caixa.

Exemplo de criação administrativa:

```json
{
  "username": "funcionario.exemplo",
  "password": "uma-senha-forte-de-12-caracteres",
  "papel": "FUNCIONARIO"
}
```

Uma edição geral altera apenas papel, vínculo e ativação; redefinição de senha não faz parte desta API.

## Identificação de clientes e veículos

Na API, o **cliente é identificado pelo CPF/CNPJ** e o **veículo pela placa**. Os ids numéricos existem apenas internamente no banco e não aparecem nas requisições nem nas respostas.

- Nos caminhos, informe o CPF/CNPJ **somente com números** (`/api/v1/clientes/52998224725`). No corpo das requisições, pontuação é aceita (`529.982.247-25`).
- A placa aceita o padrão antigo ou Mercosul, com ou sem hífen, em maiúsculas ou minúsculas (`ABC1D23`, `abc-1d23`).
- Quando há cadastros desativados com o mesmo documento ou placa, as rotas retornam o cadastro ativo.

Abertura de OS:

```json
POST /api/v1/ordens-servico
{
  "documentoCliente": "529.982.247-25",
  "placa": "ABC1D23"
}
```

Cadastro de veículo:

```json
POST /api/v1/veiculos
{
  "documentoCliente": "52998224725",
  "placa": "ABC1D23",
  "marca": "Fiat",
  "modelo": "Uno",
  "ano": 2020
}
```

A resposta da OS traz `id`, `numero` (usado na consulta pública), `documentoCliente`, `placa`, `status`, `orcamentoAprovado`, `valorTotal` e `criadaEm`.

## Endpoints principais

- `POST /api/v1/clientes`
- `GET /api/v1/clientes`
- `GET`, `PUT`, `DELETE /api/v1/clientes/{documento}` e `PATCH /api/v1/clientes/{documento}/ativacao`
- `POST /api/v1/veiculos`
- `GET /api/v1/veiculos`
- `GET`, `PUT`, `DELETE /api/v1/veiculos/{placa}` e `PATCH /api/v1/veiculos/{placa}/ativacao`
- `POST /api/v1/ordens-servico`
- `GET /api/v1/ordens-servico/{id}`
- `POST /api/v1/ordens-servico/{id}/diagnostico`
- `POST /api/v1/ordens-servico/{id}/orcamentos`
- `POST /api/v1/ordens-servico/{id}/orcamentos/{orcamentoId}/aprovar`
- `POST /api/v1/ordens-servico/{id}/orcamentos/{orcamentoId}/rejeitar`
- `GET /api/v1/public/ordens-servico/{numero}?documento=...`
- `GET /api/v1/ordens-servico/metricas/tempo-medio-execucao`

## Fluxo principal da Ordem de Serviço

1. Cliente cadastrado
2. Veículo cadastrado
3. OS criada
4. Diagnóstico registrado
5. Itens adicionados
6. Orçamento gerado e disponibilizado
7. Orçamento aprovado ou rejeitado
8. Execução da OS
9. Finalização e entrega

## Observação final

O repositório segue o roadmap definido em `.dev/plans` e já implementa a base funcional da Fase 1 com o fluxo principal da OS coberto por testes, endpoints e regras de negócio relevantes.
