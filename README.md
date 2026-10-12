# Tech Challenge FIAP

Breve descrição da solução, problema que resolve e objetivo do sistema.

---

# Integrantes

| Nome | RM |
|--------|--------|
| Integrante 1 | RM123456 |
| Integrante 2 | RM123457 |
| Integrante 3 | RM123458 |

---

# Objetivo

Sistema de gerenciamento de Ordens de Serviço para oficinas mecânicas, permitindo o cadastro de clientes, veículos, serviços, peças, geração de orçamento e acompanhamento da execução dos serviços.

---

# Arquitetura da Solução

Descrever brevemente a arquitetura utilizada.

Exemplo:

    API
    │
    ├── Application
    ├── Domain
    ├── Infrastructure
    └── Persistence

## Padrões Utilizados

- Domain-Driven Design (DDD)
- Clean Architecture
- Repository Pattern
- Dependency Injection
- JWT Authentication

---

# Tecnologias Utilizadas

## Backend

- .NET 8
- ASP.NET Core
- Entity Framework Core

## Banco de Dados

- SQL Server
- PostgreSQL

## Testes

- xUnit
- FluentAssertions
- Moq

## Ferramentas

- Docker
- Swagger/OpenAPI
- SonarQube (Opcional)

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