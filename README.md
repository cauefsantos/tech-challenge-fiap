# Tech Challenge FIAP - Oficina Mecânica

API REST para gerenciamento de uma oficina mecânica, desenvolvida em **Java 21** com **Spring Boot 4**.

> Projeto desenvolvido como parte da Fase 1 do Tech Challenge da FIAP.

## Sumário

- [Objetivo](#objetivo)
- [Contexto acadêmico](#contexto-acadêmico)
- [Status do projeto](#status-do-projeto)
- [Arquitetura da solução](#arquitetura-da-solução)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Tecnologias utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do ambiente](#configuração-do-ambiente)
- [Perfis e bancos de dados](#perfis-e-bancos-de-dados)
- [Execução local](#execução-local)
- [Execução com Docker](#execução-com-docker)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Documentação da API](#documentação-da-api)
- [Autenticação e autorização](#autenticação-e-autorização)
- [Endpoints principais](#endpoints-principais)
- [Fluxo principal da Ordem de Serviço](#fluxo-principal-da-ordem-de-serviço)
- [Testes e cobertura](#testes-e-cobertura)
- [Segurança e análise de vulnerabilidades](#segurança-e-análise-de-vulnerabilidades)
- [Documentação DDD e decisões](#documentação-ddd-e-decisões)
- [Limitações conhecidas](#limitações-conhecidas)
- [Vídeo de apresentação](#vídeo-de-apresentação)
- [Integrantes](#integrantes)
- [Referências](#referências)

---

## Objetivo

Desenvolver uma API para centralizar o gerenciamento dos atendimentos de uma oficina mecânica, permitindo cadastrar clientes e veículos, abrir Ordens de Serviço, registrar diagnósticos, adicionar serviços, peças e insumos, gerar orçamentos e acompanhar a execução dos serviços até a finalização e entrega do veículo.

A solução busca oferecer rastreabilidade ao ciclo de atendimento, apoiar a operação da oficina e manter informações de clientes, veículos, materiais, serviços, estoque, orçamentos e Ordens de Serviço em um único sistema.

## Contexto acadêmico

O projeto foi desenvolvido como parte da Fase 1 do Tech Challenge da FIAP, aplicando conceitos de Domain-Driven Design (DDD), arquitetura em camadas, APIs REST, autenticação JWT, persistência de dados, documentação OpenAPI, gerenciamento de migrations, testes automatizados e análise de vulnerabilidades.

---

## Status do projeto

A base funcional da Fase 1 está implementada, contemplando:

- persistência do agregado de Ordem de Serviço;
- regras de domínio e transições de status;
- diagnóstico e itens de serviço e material;
- geração, disponibilização, aprovação e rejeição de orçamento;
- validação e consumo de estoque durante a aprovação;
- consulta pública da Ordem de Serviço por número e documento;
- métrica de tempo médio de execução;
- autenticação e autorização por perfil;
- execução local e por Docker Compose;
- PostgreSQL como banco principal;
- testes automatizados e cobertura JaCoCo.

---

## Arquitetura da solução

A solução foi desenvolvida como uma **aplicação monolítica organizada em camadas**, utilizando princípios de **Domain-Driven Design (DDD)** para separar as regras de negócio dos detalhes técnicos. O domínio concentra entidades e regras da oficina, a camada de aplicação coordena os casos de uso, a infraestrutura implementa persistência, segurança e configurações técnicas, enquanto a apresentação disponibiliza as funcionalidades por meio de uma API REST. Essa organização favorece manutenção, testes automatizados e evolução do projeto sem introduzir a complexidade de uma arquitetura distribuída.

### Responsabilidades das camadas

- **Domain:** entidades, agregados, objetos de valor, regras de negócio e contratos de repositório.
- **Application:** casos de uso e coordenação das operações do domínio.
- **Infrastructure:** persistência JPA, banco de dados, segurança JWT e configurações técnicas.
- **Presentation:** controllers REST, DTOs, validações HTTP e tratamento de exceções.

### Fluxo de dependência

```text
Presentation
     |
     v
Application
     |
     v
Domain
     ^
     |
Infrastructure
```

O domínio não deve depender de detalhes de API, banco de dados, JWT ou classes específicas de infraestrutura.

---

## Estrutura do projeto

```text
.
├── .dev/
│   └── plans/
├── .github/
│   ├── dependabot.yml
│   └── workflows/
├── docs/
│   ├── README.md
│   ├── decisoes-fase1.md
│   ├── ddd/
│   └── security/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/fiap/oficina/
│   │   │       ├── application/
│   │   │       ├── domain/
│   │   │       ├── infrastructure/
│   │   │       └── presentation/
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
├── .env.example
├── .gitignore
├── compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Resumo das principais pastas

- **`.dev/plans/`:** planos de implementação, revisões de escopo e registros de planejamento.
- **`.github/`:** configurações de automação, atualização de dependências e pipelines.
- **`docs/`:** documentação complementar do projeto.
- **`docs/ddd/`:** Linguagem Ubíqua, Event Storming, entidades e regras do domínio.
- **`docs/security/`:** relatórios e evidências de análise de vulnerabilidades.
- **`src/main/java/`:** código-fonte principal da aplicação.
- **`application/`:** casos de uso da aplicação.
- **`domain/`:** regras de negócio independentes de frameworks e infraestrutura.
- **`infrastructure/`:** persistência, segurança e configurações técnicas.
- **`presentation/`:** endpoints REST e contratos HTTP.
- **`src/main/resources/`:** configurações, profiles e migrations do Flyway.
- **`src/test/java/`:** testes automatizados.
- **`compose.yml`:** definição da API e do PostgreSQL para execução coordenada.
- **`Dockerfile`:** construção da imagem da aplicação.
- **`pom.xml`:** dependências, plugins e configurações do Maven.

> Ajuste a árvore acima caso alguma pasta ainda não exista no repositório.

---

## Tecnologias utilizadas

### Linguagem e framework

- **Java 21:** linguagem principal do projeto.
- **Spring Boot 4:** inicialização, configuração e execução da aplicação.
- **Spring Framework 7:** base do ecossistema Spring utilizado pelo Spring Boot.
- **Maven Wrapper:** build padronizado sem exigir Maven instalado globalmente.

### API e validação

- **Spring Web MVC:** criação dos endpoints REST.
- **Jakarta Validation:** validação dos dados de entrada.
- **Jackson:** serialização e desserialização JSON.
- **Springdoc OpenAPI:** documentação OpenAPI e Swagger UI.

### Persistência

- **Spring Data JPA:** abstração da camada de persistência.
- **Hibernate:** mapeamento objeto-relacional.
- **PostgreSQL:** banco de dados relacional principal.
- **H2:** banco em memória para cenários locais e testes definidos pelo projeto.
- **Flyway:** criação e evolução versionada do schema.
- **HikariCP:** pool de conexões com o banco.

### Segurança

- **Spring Security:** autenticação e autorização.
- **JWT:** autenticação stateless baseada em token.
- **BCrypt:** hash seguro das senhas.

### Testes e qualidade

- **JUnit 5:** testes automatizados.
- **Mockito:** mocks utilizados nos testes unitários.
- **JaCoCo:** métricas e gates de cobertura.
- **OWASP Dependency Check:** análise de vulnerabilidades das dependências.
- **Trivy:** análise da imagem Docker.
- **Dependabot:** monitoramento e atualização de dependências.

### Infraestrutura

- **Docker:** construção e execução da imagem da API.
- **Docker Compose:** inicialização coordenada da API e do PostgreSQL.

---

## Pré-requisitos

### Execução local

- Java 21;
- Git;
- Maven Wrapper incluído no repositório;
- PostgreSQL, caso o profile selecionado utilize esse banco.

### Execução em contêineres

- Docker Engine;
- Docker Compose 2.17 ou superior.

### Verificação

```bash
java -version
git --version
docker --version
docker compose version
```

No Linux ou macOS:

```bash
./mvnw --version
```

No Windows:

```powershell
.\mvnw.cmd --version
```

---

## Configuração do ambiente

### 1. Criar o arquivo `.env`

No Linux ou macOS:

```bash
cp .env.example .env
```

No PowerShell:

```powershell
Copy-Item .env.example .env
```

Edite o `.env` e substitua todos os valores de exemplo antes de iniciar a aplicação.

### 2. Variáveis esperadas

- `JWT_SECRET`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_PORT`
- `API_PORT`
- `SERVER_PORT`
- `SPRING_PROFILES_ACTIVE`
- `JWT_TOKEN_VALIDITY`
- `DB_POOL_MIN_IDLE`
- `DB_POOL_MAX_SIZE`
- `DB_CONNECTION_TIMEOUT_MS`
- `DB_VALIDATION_TIMEOUT_MS`
- `DB_IDLE_TIMEOUT_MS`
- `DB_MAX_LIFETIME_MS`
- `FLYWAY_CONNECT_RETRIES`
- `OPENAPI_ENABLED`
- `SWAGGER_UI_ENABLED`

Gere uma chave JWT exclusiva:

```bash
openssl rand -base64 32
```

> O valor de `JWT_SECRET` deve representar pelo menos 32 bytes após a decodificação Base64.

### 3. Carregar variáveis no terminal

#### PowerShell

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
. .\set-env.ps1
.\mvnw.cmd spring-boot:run
```

#### Prompt de Comando

```bat
call set-env.bat
mvnw.cmd spring-boot:run
```

O `set-env.bat` configura somente o processo atual do **Prompt de Comando (cmd.exe)**; executá-lo pelo PowerShell, IntelliJ ou outro processo filho não consegue alterar as variáveis do processo que o iniciou. No PowerShell, carregue o script no próprio terminal com `. .\set-env.ps1` antes de iniciar a aplicação. No Prompt de Comando, use `call set-env.bat` e inicie o Maven na mesma janela. Para conferir sem revelar valores, execute `if defined JWT_SECRET (echo JWT_SECRET carregado) else (echo JWT_SECRET ausente)` no CMD.

#### Bash

```bash
source ./set-env.sh
./mvnw spring-boot:run
```

Os scripts configuram somente o terminal atual e não devem exibir os valores sensíveis.

### Segurança das configurações

- Não versione o arquivo `.env`.
- Mantenha `.env` no `.gitignore`.
- Versione apenas `.env.example`, sem valores reais.
- Não reutilize credenciais locais em produção.
- Em cloud, utilize o mecanismo de secrets da plataforma.

---

## Perfis e bancos de dados

### Profile `dev`

Indicado para desenvolvimento local. Dependendo da configuração do projeto, pode usar H2 em memória ou PostgreSQL local.

Execute explicitamente com:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

### Profile `prod`

Indicado para execução com PostgreSQL e configurações externalizadas. Não deve conter senhas ou chaves padrão.

```bash
java -jar target/ARQUIVO_DA_APLICACAO.jar \
  --spring.profiles.active=prod
```

### Ambientes de banco

- **H2:** execução rápida e cenários de teste definidos pelo projeto.
- **PostgreSQL local:** desenvolvimento integrado.
- **PostgreSQL via Docker Compose:** ambiente local próximo ao produtivo.
- **PostgreSQL externo ou gerenciado:** produção e cloud.

> Confirme no `application-dev.yml` se o profile `dev` utiliza H2 ou PostgreSQL por padrão. A documentação deve refletir exatamente a configuração vigente.

---

## Execução local

### Executar os testes

No Linux ou macOS:

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

### Iniciar pelo Maven Wrapper

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### Gerar o JAR

```bash
./mvnw clean package
```

No Windows:

```powershell
.\mvnw.cmd clean package
```

### Executar o JAR

```bash
java -jar target/ARQUIVO_DA_APLICACAO.jar \
  --spring.profiles.active=dev
```

Substitua `ARQUIVO_DA_APLICACAO.jar` pelo nome realmente gerado no diretório `target`.

---

## Execução com Docker

A execução com Docker Compose inicia a API e o PostgreSQL como serviços separados. A API utiliza o profile `prod`, recebe as variáveis de ambiente definidas no `.env` e aguarda o banco ficar saudável.

### Subir os serviços

```bash
docker compose up --build --wait
```

### Verificar o estado

```bash
docker compose ps
```

### Acompanhar os logs da API

```bash
docker compose logs -f api
```

### Acompanhar todos os logs

```bash
docker compose logs -f
```

### Parar sem apagar o banco

```bash
docker compose down
```

### Parar e apagar o volume local

```bash
docker compose down -v
```

> O comando `docker compose down -v` remove o volume do PostgreSQL e apaga os dados locais.

### Portas padrão

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

Se as portas estiverem ocupadas, ajuste `API_PORT` e `DB_PORT` no `.env`.

---

## Banco de dados e migrations

O PostgreSQL foi escolhido por oferecer suporte a transações, integridade referencial, restrições relacionais e consistência dos dados. Essas características são importantes para as operações de Ordem de Serviço, orçamento, usuários e estoque.

O H2 é utilizado apenas nos cenários definidos para desenvolvimento rápido ou testes. O PostgreSQL representa o ambiente principal de execução integrada e produção.

O Flyway é responsável pela criação e evolução do schema. As migrations estão localizadas em:

```text
src/main/resources/db/migration/
```

O padrão dos nomes é:

```text
V<versão>__<descrição>.sql
```

Exemplo:

```text
V1__create_ordem_servico.sql
```

O Hibernate deve permanecer com `ddl-auto: validate`, usando o schema criado pelas migrations.

### Verificar as tabelas no Compose

```bash
docker compose exec postgres \
  psql -U "$DB_USERNAME" -d oficina -c "\dt"
```

### Consultar o histórico do Flyway

```bash
docker compose exec postgres \
  psql -U "$DB_USERNAME" -d oficina \
  -c "SELECT version, description, script, success FROM flyway_schema_history ORDER BY installed_rank;"
```

---

## Documentação da API

Com a aplicação em execução e a documentação habilitada:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML versionado: [docs/api/openapi.yaml](docs/api/openapi.yaml). Para atualizá-lo, inicie a aplicação com OpenAPI habilitado e execute `mvn verify -Popenapi`; o CI padrão não ativa esse profile.

Em produção, Swagger e OpenAPI devem permanecer desabilitados por padrão, salvo decisão consciente da equipe e proteção adequada do acesso.

---

## Autenticação e autorização

A API utiliza JWT emitido pelo endpoint:

```http
POST /api/v1/auth/login
```

O administrador inicial é criado apenas quando o username ainda não existe. Reinícios não redefinem sua senha. A aplicação deve falhar ao iniciar quando as credenciais obrigatórias de bootstrap ou o segredo JWT não forem fornecidos.

### Perfis

#### `ADMIN`

- Gerencia usuários.
- Acessa os endpoints autenticados de clientes, veículos, serviços, materiais e Ordens de Serviço.

#### `FUNCIONARIO`

- Acessa os endpoints operacionais.
- Não pode gerenciar usuários.

#### `CLIENTE`

- Não possui endpoints autenticados de recursos nesta fase.
- Pode utilizar a consulta pública da Ordem de Serviço por número e documento.

### Criação administrativa de usuário

```http
POST /api/v1/usuarios
Content-Type: application/json
Authorization: Bearer TOKEN_ADMIN
```

```json
{
  "username": "funcionario.exemplo",
  "password": "uma-senha-forte-de-12-caracteres",
  "papel": "FUNCIONARIO"
}
```

Regras relevantes:

- usernames são normalizados para minúsculas;
- usernames são únicos sem distinção de caixa;
- senhas ou hashes nunca aparecem nas respostas;
- contas `CLIENTE` exigem `documentoCliente` associado a um cliente cadastrado;
- a exclusão administrativa desativa a conta.

---

## Identificação de clientes e veículos

- Clientes são identificados externamente por CPF ou CNPJ.
- Veículos são identificados externamente pela placa.
- IDs numéricos são internos e não devem aparecer nos contratos externos quando a regra do projeto define documento ou placa como identificador público.

### Consultar cliente

```http
GET /api/v1/clientes/52998224725
```

Nos caminhos, informe CPF ou CNPJ somente com números. Nos corpos de requisição, a pontuação pode ser aceita conforme a validação implementada.

### Abrir Ordem de Serviço

```http
POST /api/v1/ordens-servico
Content-Type: application/json
Authorization: Bearer TOKEN
```

```json
{
  "documentoCliente": "529.982.247-25",
  "placa": "ABC1D23"
}
```

### Cadastrar veículo

```http
POST /api/v1/veiculos
Content-Type: application/json
Authorization: Bearer TOKEN
```

```json
{
  "documentoCliente": "52998224725",
  "placa": "ABC1D23",
  "marca": "Fiat",
  "modelo": "Uno",
  "ano": 2020
}
```

A placa pode aceitar o padrão antigo ou Mercosul, com ou sem hífen, conforme a validação implementada.

---

## Endpoints principais

### Autenticação e usuários

- `POST /api/v1/auth/login`
- `POST /api/v1/usuarios`
- `GET /api/v1/usuarios`
- `GET /api/v1/usuarios/{id}`
- `PUT /api/v1/usuarios/{id}`
- `DELETE /api/v1/usuarios/{id}`

### Clientes

- `POST /api/v1/clientes`
- `GET /api/v1/clientes`
- `GET /api/v1/clientes/{documento}`
- `PUT /api/v1/clientes/{documento}`
- `DELETE /api/v1/clientes/{documento}`
- `PATCH /api/v1/clientes/{documento}/ativacao`

### Veículos

- `POST /api/v1/veiculos`
- `GET /api/v1/veiculos`
- `GET /api/v1/veiculos/{placa}`
- `PUT /api/v1/veiculos/{placa}`
- `DELETE /api/v1/veiculos/{placa}`
- `PATCH /api/v1/veiculos/{placa}/ativacao`

### Materiais

- `POST /api/v1/materiais`
- `GET /api/v1/materiais`
- `GET /api/v1/materiais/{id}`
- `PUT /api/v1/materiais/{id}`
- `POST /api/v1/materiais/{id}/entradas-estoque`
- `DELETE /api/v1/materiais/{id}`
- `PATCH /api/v1/materiais/{id}/ativacao`

### Ordens de Serviço

- `POST /api/v1/ordens-servico`
- `GET /api/v1/ordens-servico/{id}`
- `POST /api/v1/ordens-servico/{id}/diagnostico`
- `POST /api/v1/ordens-servico/{id}/orcamentos`
- `POST /api/v1/ordens-servico/{id}/orcamentos/{orcamentoId}/disponibilizar`
- `POST /api/v1/ordens-servico/{id}/orcamentos/{orcamentoId}/aprovar`
- `POST /api/v1/ordens-servico/{id}/orcamentos/{orcamentoId}/rejeitar`
- `GET /api/v1/public/ordens-servico/{numero}?documento=...`
- `GET /api/v1/ordens-servico/metricas/tempo-medio-execucao`

A relação completa e atualizada dos endpoints deve ser consultada no Swagger.

---

## Fluxo principal da Ordem de Serviço

```text
Cliente cadastrado
        |
        v
Veículo cadastrado
        |
        v
Ordem de Serviço criada
        |
        v
Diagnóstico registrado
        |
        v
Serviços e materiais adicionados
        |
        v
Orçamento gerado e disponibilizado
        |
        v
Orçamento aprovado ou rejeitado
        |
        v
Execução da Ordem de Serviço
        |
        v
Finalização
        |
        v
Entrega do veículo
```

### Status previstos

```text
RECEBIDA
EM_DIAGNOSTICO
AGUARDANDO_APROVACAO
EM_EXECUCAO
FINALIZADA
ENTREGUE
```

As transições devem respeitar as regras do domínio e gerar histórico quando aplicável.

### Orçamento: gerar, disponibilizar e aprovar

1. `POST /api/v1/ordens-servico/{id}/orcamentos` gera o orçamento e devolve o seu `id`, com os valores de serviços, peças, insumos e o total.
2. `POST /api/v1/ordens-servico/{id}/orcamentos/{orcamentoId}/disponibilizar` envia o orçamento para aprovação. A OS passa para `AGUARDANDO_APROVACAO`.
3. `POST /api/v1/ordens-servico/{id}/orcamentos/{orcamentoId}/aprovar` (ou `/rejeitar`) registra a decisão do cliente.

O mesmo `orcamentoId` é usado nos três passos. As respostas da OS (`GET /api/v1/ordens-servico/{id}` e as operações do fluxo) trazem `numero`, `diagnostico`, `itensServico`, `itensMaterial` e `orcamentos`, cada item com o seu `id`. Esses ids são usados, por exemplo, em `DELETE /api/v1/ordens-servico/{id}/itens-servico/{itemId}`.

---

## Testes e cobertura

Use Java 21 para executar a suíte.

### Windows PowerShell

```powershell
$env:JAVA_HOME = 'C:\caminho\para\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
.\mvnw.cmd clean verify
```

### Linux ou macOS

```bash
./mvnw clean verify
```

O comando `verify`:

- executa a suíte automatizada;
- gera o relatório JaCoCo;
- exige cobertura mínima de 80% das instruções em `domain.*`;
- exige cobertura mínima de 80% das instruções em `application.*`.

Relatório HTML:

```text
target/site/jacoco/index.html
```

### Último resultado registrado

Última execução registrada em **28/10/2026**, com **JDK 21.0.2**:

| Resultado | Valor |
|---|---:|
| Testes executados | 175 |
| Falhas | 0 |
| Erros | 0 |
| Testes ignorados | 0 |
| Cobertura de instruções em `domain.*` | 95,91% |
| Cobertura de instruções em `application.*` | 97,16% |
| Build e gates JaCoCo | Aprovados |

Os percentuais informados não incluem `infrastructure` nem `presentation`. Testes de integração envolvendo persistência e comportamento transacional são complementares aos testes unitários e mocks.

O [relatório JaCoCo HTML arquivado](docs/relatorio-cobertura/index.html) permite consultar a evidência registrada sem executar o projeto. Uma nova execução gera o relatório em `target/site/jacoco/index.html`; para atualizar a cópia versionada, siga `docs/relatorio-cobertura/README.md`. Os percentuais não incluem `infrastructure` nem `presentation`.

Execute novamente o comando para atualizar o relatório e os números antes da entrega.

---

## Segurança e análise de vulnerabilidades

A solução utiliza:

- autenticação e autorização JWT;
- senhas protegidas com BCrypt;
- autorização explícita por perfil;
- validação de CPF, CNPJ e placa;
- configurações sensíveis externalizadas;
- JPA para acesso estruturado ao banco;
- testes dos fluxos críticos;
- execução da imagem sem privilégios de root;
- `.dockerignore` para excluir arquivos desnecessários e sensíveis do contexto de build.

### Ferramentas

- GitHub Dependabot;
- OWASP Dependency Check;
- Trivy para a imagem Docker.

### Relatórios e evidências

Substitua os caminhos abaixo pelos arquivos ou links reais do repositório e do pipeline:

- `docs/security/dependency-check-report.md`
- `docs/security/trivy-report.md`
- `.github/dependabot.yml`
- `.github/workflows/security.yml`
- `LINK_DOS_ARTEFATOS_DO_PIPELINE`

> Não declare que um relatório está disponível se o arquivo ou artefato ainda não existir. Antes da entrega, valide todos os links.

---

## Documentação DDD e decisões

### Artefatos da Fase 1

Substitua ou ajuste os caminhos conforme a estrutura real do repositório:

- [Documentação adcional do projeto](docs/README.md)
- [Decisões da Fase 1](docs/decisoes-fase1.md)
- [Linguagem Ubíqua](docs/ddd/linguagem-ubiqua.md)
- [Event Storming](docs/ddd/event-storming.md)
- [Entidades da Fase 1](docs/ddd/entidades-fase1.md)
- [Regras de negócio](docs/ddd/regras-de-negocio.md)

### Documentos de planejamento

- `.dev/plans/00-plano-fase1-original-extraido.md`
- `.dev/plans/01-analise-miro-fase01.md`
- `.dev/plans/02-plano-fase1-revisado.md`

### Decisões do MVP

- aprovação do orçamento autenticada pelo sistema;
- estoque validado e consumido durante a aprovação;
- Ordem de Serviço alterada para cancelada após rejeição, conforme regra vigente no projeto;
- itens de serviço e material adicionados somente durante o diagnóstico;
- diagnóstico mantido como atributo da Ordem de Serviço;
- peças e insumos representados pelo conceito de `Material`.

---

## Limitações conhecidas

Os comportamentos abaixo foram identificados durante os testes e devem ser acompanhados por issues ou tarefas técnicas antes da entrega final:

1. `HistoricoStatus.setId` não atualiza o identificador.
2. `OrdemServico.concluirDiagnostico` valida o diagnóstico, mas não altera o status nem registra o histórico.
3. Uma falha de validação durante a disponibilização pode deixar o orçamento como `DISPONIBILIZADO` enquanto a Ordem de Serviço permanece em diagnóstico.

### Ações recomendadas

- criar uma issue para cada comportamento;
- classificar a severidade e o impacto;
- priorizar a correção da inconsistência transacional entre orçamento e Ordem de Serviço;
- adicionar testes de regressão;
- atualizar esta seção após as correções.

Links das issues:

- `LINK_ISSUE_HISTORICO_STATUS`
- `LINK_ISSUE_CONCLUSAO_DIAGNOSTICO`
- `LINK_ISSUE_CONSISTENCIA_ORCAMENTO_OS`

> Caso as falhas sejam corrigidas antes da entrega, remova esta seção ou substitua pelo histórico das correções.

---

## Vídeo de apresentação

Adicione o link público da apresentação da Fase 1:

- `LINK_PUBLICO_DO_VIDEO`

Antes da entrega, valide:

- se o vídeo está acessível sem solicitação de permissão;
- se a versão demonstrada corresponde ao commit entregue;
- se o tempo está dentro do limite definido pela Fase 1.

---

## Integrantes

| Nome | RM | Discord |
|---|---|---|
| NOME_DO_INTEGRANTE | RM000000 | USUARIO_DISCORD |
| NOME_DO_INTEGRANTE | RM000000 | USUARIO_DISCORD |
| NOME_DO_INTEGRANTE | RM000000 | USUARIO_DISCORD |

Informe também no PDF final o número do grupo, a branch e o hash do commit entregue.

---

## Referências

- Documentação oficial do Java 21
- Documentação oficial do Spring Boot
- Documentação oficial do Spring Security
- Documentação oficial do Spring Data JPA
- Documentação oficial do PostgreSQL
- Documentação oficial do Flyway
- Documentação oficial do Docker
- Especificação OpenAPI
- Material da Fase 1 do Tech Challenge FIAP

---

## Observação final

O projeto implementa o núcleo funcional da Fase 1, com fluxo de Ordem de Serviço, regras de domínio, autenticação, persistência, migrations, execução em contêineres e testes automatizados. Antes da entrega, todos os placeholders, links, caminhos de documentos, resultados de testes e limitações conhecidas devem ser revisados para refletir exatamente o conteúdo do repositório e a versão identificada pelo commit entregue.
