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

A segurança da aplicação é tratada por meio da autenticação baseada em JWT para acesso aos endpoints administrativos, garantindo que apenas usuários autenticados possam executar operações protegidas.

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

Variáveis esperadas:

- `JWT_SECRET`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`

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

A API usa JWT. O fluxo principal envolve login do usuário, geração do token e envio do header `Authorization` nas requisições autenticadas.

## Endpoints principais

- `POST /api/v1/clientes`
- `GET /api/v1/clientes`
- `POST /api/v1/veiculos`
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
