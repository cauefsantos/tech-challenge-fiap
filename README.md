# Tech Challenge FIAP

Projeto desenvolvido para a Fase 1 do Tech Challenge do curso de Pós em arquitetura de software.

---

# Integrantes

| Nome | RM |
|--------|--------|
| Caue Santos | RM123456 |
| Gabriel da Costa Silva | RM123457 |
| Guilherme Mateus | RM1234567 |
| Renato Novais | RM123458 |
| Ricardo Barbosa | RM1234567 |

---

# Objetivo

A solução tem como objetivo apoiar a operação de uma oficina mecânica por meio da centralização e rastreabilidade dos atendimentos realizados. O sistema permite registrar clientes e veículos, abrir Ordens de Serviço, documentar diagnósticos, adicionar serviços, peças e insumos, calcular e disponibilizar orçamentos e acompanhar a evolução do atendimento até a finalização e entrega do veículo.

# Descrição

Como parte da Fase 1 do Tech Challenge, o projeto também busca demonstrar a aplicação prática de conceitos de arquitetura de software, Domain-Driven Design (DDD), segurança, persistência de dados, documentação de APIs e qualidade de software.

---

# Arquitetura da Solução

A solução foi desenvolvida como uma aplicação monolítica organizada em camadas, utilizando princípios de Domain-Driven Design (DDD) para separar as regras de negócio dos detalhes técnicos da aplicação. A arquitetura divide o sistema entre as camadas de domínio, aplicação, infraestrutura e apresentação, permitindo que cada parte possua responsabilidades bem definidas. 

O domínio concentra as entidades e regras da oficina, a aplicação coordena os casos de uso, a infraestrutura implementa persistência, segurança e configurações técnicas, enquanto a apresentação disponibiliza as funcionalidades por meio de uma API REST. Essa organização favorece a manutenção, os testes automatizados e a evolução do projeto sem introduzir a complexidade de uma arquitetura distribuída.

## Fluxo de dependencia entre as camadas
```
Presentation
      │
      ▼
Application
      │
      ▼
Domain
      ▲
      │
Infrastructure
```

## Estrutura de pastas
```
oficina-api/
├── docs/
│   ├── ddd/
│   ├── diagrams/
│   └── security/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/fiap/oficina/
│   │   │       ├── OficinaApplication.java
│   │   │       ├── domain/
│   │   │       ├── application/
│   │   │       ├── infrastructure/
│   │   │       └── presentation/
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   │
│   └── test/
│       └── java/
│           └── br/com/fiap/oficina/
│               ├── unit/
│               ├── integration/
│               └── architecture/
│
├── .env.example
├── .gitignore
├── compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Padrões Utilizados

- Domain-Driven Design (DDD)
- Clean Architecture
- Repository Pattern
- Dependency Injection
- JWT Authentication

---

# Tecnologias Utilizadas

A aplicação foi desenvolvida em Java utilizando o ecossistema Spring para construção da API REST, implementação da segurança, persistência dos dados e organização das regras da aplicação. O PostgreSQL foi adotado como banco de dados relacional, enquanto o Flyway é responsável pelo versionamento e pela evolução do schema. O projeto também utiliza Docker para padronizar o ambiente de execução e ferramentas de testes e cobertura para apoiar a qualidade do código.

## Linguagem e framework
- Java 21: linguagem principal utilizada no desenvolvimento da aplicação.
- Spring Boot 4: responsável pela configuração, inicialização e execução da aplicação.
- Spring Framework 7: base do ecossistema Spring utilizado pelo Spring Boot 4.
- Maven: gerenciamento de dependências, compilação, testes e empacotamento do projeto.
## API e validação
- Spring Web MVC: criação dos controllers e endpoints REST.
- Jakarta Validation: validação dos dados recebidos pela API.
- Jackson: serialização e desserialização de objetos JSON.
- Springdoc OpenAPI: geração da especificação OpenAPI e disponibilização do Swagger UI.
## Persistência
- Spring Data JPA: abstração da camada de acesso aos dados.
- Hibernate: implementação de persistência e mapeamento objeto-relacional.
- PostgreSQL: banco de dados relacional utilizado pela aplicação.
- Flyway: criação, versionamento e evolução controlada do schema do banco de dados.
- HikariCP: gerenciamento do pool de conexões com o banco.
## Segurança
- Spring Security: autenticação e autorização dos acessos à API.
- OAuth2 Resource Server: validação das requisições autenticadas com token.
- JWT: geração e utilização de tokens para autenticação stateless.
- BCrypt: geração segura do hash das senhas.
## Testes e qualidade
- JUnit 5: execução dos testes automatizados.
- Mockito: criação de objetos simulados nos testes unitários.
- Spring Security Test: suporte aos testes de autenticação e autorização.
- JaCoCo: geração do relatório de cobertura dos testes.
## Infraestrutura
- Docker: construção e execução da imagem da aplicação.
- Docker Compose: inicialização coordenada da API e do PostgreSQL.
- Maven Wrapper: execução padronizada do Maven sem exigir uma instalação global.

---

# Estrutura do Projeto

    src/
    │
    ├── Api
    ├── Application
    ├── Domain
    ├── Infrastructure
    └── Tests

---

# Decisões Arquiteturais

## Arquitetura em Camadas

A solução foi organizada utilizando separação de responsabilidades entre as camadas:

- API
- Application
- Domain
- Infrastructure

## DDD

Os conceitos de Domain-Driven Design foram utilizados para organizar:

- Linguagem Ubíqua
- Entidades
- Value Objects
- Regras de Negócio
- Aggregate Root

---

# Requisitos

## Pré-requisitos

- .NET SDK 8
- Docker Desktop
- Banco de Dados SQL Server ou PostgreSQL
- Git

---

# Configuração do Ambiente

## Clonar Repositório

```bash
git clone https://github.com/seu-usuario/seu-repositorio.git

cd seu-repositorio
```

## Configurar Variáveis de Ambiente

Exemplo:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost;Database=Oficina;"
  },
  "Jwt": {
    "Key": "MinhaChaveSecreta"
  }
}
```

---

# Executando o Projeto

## Restaurar Dependências

```bash
dotnet restore
```

## Compilar

```bash
dotnet build
```

## Executar Migrações

```bash
dotnet ef database update
```

## Executar Aplicação

```bash
dotnet run
```

---

# Executando com Docker

## Construir Imagens

```bash
docker compose build
```

## Subir Ambiente

```bash
docker compose up -d
```

## Derrubar Ambiente

```bash
docker compose down
```

---

# Banco de Dados

## Justificativa

Descrever os motivos da escolha do banco utilizado.

## Entidades Principais

- Cliente
- Veículo
- Ordem de Serviço
- Serviço
- Peça
- Insumo

---

# Autenticação

A API utiliza autenticação JWT.

## Fluxo

1. Usuário realiza login.
2. API gera Token JWT.
3. Token é enviado nas chamadas autenticadas.

Exemplo:

```http
Authorization: Bearer {token}
```

---

# Endpoints Principais

| Método | Endpoint | Descrição |
|----------|----------|----------|
| POST | /clientes | Criar cliente |
| GET | /clientes | Listar clientes |
| POST | /veiculos | Criar veículo |
| POST | /ordens-servico | Criar ordem de serviço |
| GET | /ordens-servico | Consultar ordens de serviço |

---

# Swagger

Após iniciar a aplicação:

```text
http://localhost:5000/swagger
```

ou

```text
https://localhost:5001/swagger
```

---

# Fluxos de Negócio

## Fluxo Principal da Ordem de Serviço

    Cliente Identificado
            ↓
    Veículo Cadastrado
            ↓
    Ordem de Serviço Criada
            ↓
    Diagnóstico Realizado
            ↓
    Serviços Adicionados
            ↓
    Peças Adicionadas
            ↓
    Orçamento Calculado
            ↓
    Orçamento Aprovado
            ↓
    Execução dos Serviços
            ↓
    Ordem de Serviço Finalizada
            ↓
    Veículo Entregue

---

# Testes

## Executar Todos os Testes

```bash
dotnet test
```

## Cobertura

```bash
dotnet test --collect:"XPlat Code Coverage"
```

---

# Cobertura de Testes

Cobertura mínima esperada:

```text
80% ou superior
```

Inserir relatório ou imagem gerada pela ferramenta utilizada.

---

# Documentação DDD

Link da documentação:

```text
https://link-da-documentacao
```

Conteúdo:

- Linguagem Ubíqua
- Event Storming
- Bounded Contexts
- Entidades
- Value Objects
- Aggregate Root
- Regras de Negócio
- Diagrama de Estados

---

# Análise de Vulnerabilidades

Ferramenta utilizada:

```text
OWASP Dependency Check
```

ou

```text
Snyk
```

Resultado:

```text
Nenhuma vulnerabilidade crítica encontrada.
```

Inserir evidências e relatórios quando disponíveis.

---

# CI/CD

Caso o projeto utilize pipeline:

- Build Automatizado
- Execução de Testes
- Validação de Qualidade
- Publicação Automatizada

Exemplo:

- GitHub Actions
- Azure DevOps
- GitLab CI/CD

---

# Vídeo de Apresentação

Link da demonstração:

```text
https://youtube.com/...
```

---

# Referências

- Tech Challenge Fase 1
- Documentação Oficial .NET
- Documentação ASP.NET Core
- Documentação Entity Framework Core
- Documentação Swagger/OpenAPI

---

# Considerações Finais

Este projeto foi desenvolvido como parte da Fase 1 do Tech Challenge utilizando conceitos de Arquitetura de Software, Domain-Driven Design, APIs REST, autenticação JWT, testes automatizados e boas práticas de desenvolvimento moderno.