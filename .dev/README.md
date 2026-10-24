# Guia de Desenvolvimento e Execução

## Oficina API

Este documento orienta o ambiente de desenvolvimento da **Oficina API**, incluindo a preparação do PostgreSQL, execução por perfis, uso do Maven, criação da imagem Docker e inicialização com Docker Compose.

---

## 1. Tecnologias e ferramentas

O projeto utiliza:

- Java 21
- Spring Boot 4
- Spring Data JPA
- Spring Security com JWT
- PostgreSQL
- Flyway
- Maven
- Docker
- Docker Compose
- Swagger/OpenAPI
- JaCoCo

---

## 2. Estrutura esperada do projeto

    oficina-api/
    ├── docs/
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   └── resources/
    │   │       ├── db/
    │   │       │   └── migration/
    │   │       ├── application.yml
    │   │       ├── application-dev.yml
    │   │       └── application-prod.yml
    │   └── test/
    ├── .env.example
    ├── compose.yml
    ├── Dockerfile
    ├── pom.xml
    └── README.md

---

## 3. Pré-requisitos

### Execução local

Instale:

- Java 21
- Maven 3.6.3 ou superior, ou Maven Wrapper do projeto
- PostgreSQL 15 ou superior
- Git

Verifique as instalações:

```bash
java -version
mvn -version
git --version
psql --version
```

Se o projeto possuir Maven Wrapper, prefira:

```bash
./mvnw -version
```

No Windows PowerShell:

```powershell
.\mvnw.cmd -version
```

### Execução com contêineres

Instale:

- Docker
- Docker Compose

Verifique:

```bash
docker --version
docker compose version
```

---

## 4. Clonar o projeto

```bash
git clone URL_DO_REPOSITORIO
cd oficina-api
```

---

## 5. Perfis da aplicação

O projeto utiliza arquivos separados por ambiente:

- `application.yml`: configurações compartilhadas e definição do profile padrão.
- `application-dev.yml`: configurações para desenvolvimento local.
- `application-prod.yml`: configurações seguras para produção.

### Profile padrão

O `application.yml` pode definir `dev` como profile padrão:

```yaml
spring:
  application:
    name: oficina-api
  profiles:
    default: dev
```

O profile padrão só é usado quando nenhum profile ativo é informado explicitamente.

### Ordem de precedência

Uma configuração externa, variável de ambiente ou argumento de linha de comando pode sobrescrever valores dos arquivos YAML. Portanto, confira variáveis antigas no terminal, IDE, Docker Compose ou sistema operacional quando uma configuração não se comportar como esperado.

---

## 6. Banco PostgreSQL para desenvolvimento

Há três formas comuns de disponibilizar o banco:

1. PostgreSQL instalado localmente.
2. PostgreSQL executado em Docker, com a aplicação executada localmente.
3. PostgreSQL e aplicação executados juntos pelo Docker Compose.

---

## 7. Criar o banco no PostgreSQL local

Entre no PostgreSQL com um usuário administrativo:

```bash
psql -h localhost -p 5432 -U postgres
```

Crie a role da aplicação:

```sql
CREATE ROLE oficina
    WITH LOGIN
    PASSWORD 'oficina';
```

Crie o banco e defina seu proprietário:

```sql
CREATE DATABASE oficina
    WITH OWNER = oficina
    ENCODING = 'UTF8';
```

Conecte-se ao banco:

```sql
\c oficina
```

Garanta privilégios no schema `public`:

```sql
GRANT USAGE, CREATE ON SCHEMA public TO oficina;
ALTER SCHEMA public OWNER TO oficina;
```

Saia do `psql`:

```sql
\q
```

### Alternativa pela linha de comando

```bash
createuser -h localhost -p 5432 -U postgres -P oficina
createdb -h localhost -p 5432 -U postgres -O oficina oficina
```

### Testar a conexão

```bash
psql -h localhost -p 5432 -U oficina -d oficina
```

Dentro do `psql`:

```sql
SELECT current_database(), current_user, current_schema();
```

---

## 8. Configuração do profile `dev`

Exemplo de `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:h2:mem:testdb}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:}

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration/{vendor}
    baseline-on-migrate: false
    validate-on-migrate: true

logging:
  level:
    org.flywaydb: DEBUG
    org.hibernate.SQL: DEBUG
```

### Responsabilidade do Flyway e do Hibernate

- O Flyway cria e evolui o schema por meio dos scripts versionados.
- O Hibernate, com `ddl-auto: validate`, apenas verifica se o schema corresponde às entidades.
- Não use `ddl-auto: create`, `create-drop` ou `update` como solução permanente quando o schema for gerenciado pelo Flyway.

### Dependências do Flyway no Spring Boot 4

O `pom.xml` deve conter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Localização das migrations

As migrations são selecionadas automaticamente pelo tipo de banco:

    src/main/resources/db/migration/h2/
    src/main/resources/db/migration/postgresql/

O placeholder `{vendor}` do Flyway resolve o diretório como `h2` ou `postgresql`, de acordo com a conexão JDBC. Os dois scripts V1 preservam as mesmas regras; no H2, colunas calculadas e índices únicos substituem os índices parciais usados no PostgreSQL.

Para usar o PostgreSQL no profile `dev`, defina as variáveis antes de iniciar a aplicação:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/oficina"
$env:DB_USERNAME = "oficina"
$env:DB_PASSWORD = "oficina"
./mvnw.cmd clean package
```

Sem essas variáveis, o profile `dev` usa o H2 em memória.

Os nomes seguem o padrão `V<versão>__<descrição>.sql`, com dois underscores entre a versão e a descrição.

---

## 9. Executar a aplicação localmente no profile `dev`

### Usando o profile padrão

Se `dev` estiver definido como profile padrão:

```bash
mvn spring-boot:run
```

Com Maven Wrapper:

```bash
./mvnw spring-boot:run
```

### Informando o profile explicitamente

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Com Maven Wrapper:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Usando variáveis de ambiente

Linux ou macOS:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/oficina
export DB_USERNAME=oficina
export DB_PASSWORD=oficina
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/oficina"
$env:DB_USERNAME="oficina"
$env:DB_PASSWORD="oficina"
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

### Usando argumentos do Spring

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--spring.profiles.active=dev --server.port=8080"
```

---

## 10. Compilar, testar e empacotar

### Limpar e compilar

```bash
./mvnw clean compile
```

### Executar testes

```bash
./mvnw test
```

### Executar verificações e gerar cobertura

```bash
./mvnw clean verify
```

O relatório do JaCoCo fica normalmente em:

    target/site/jacoco/index.html

### Gerar o arquivo JAR

```bash
./mvnw clean package
```

Arquivo esperado:

    target/oficina-api-0.0.1-SNAPSHOT.jar

### Executar o JAR no profile `dev`

```bash
java -jar target/oficina-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev
```

---

## 11. Subir apenas o PostgreSQL com Docker Compose

Essa opção é útil quando o banco roda em contêiner, mas a aplicação roda pela IDE ou Maven.

Exemplo de `compose.yml`:

```yaml
services:
  postgres:
    image: postgres:17-alpine
    container_name: oficina-postgres
    environment:
      POSTGRES_DB: oficina
      POSTGRES_USER: oficina
      POSTGRES_PASSWORD: oficina
    ports:
      - "5432:5432"
    volumes:
      - oficina_pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U oficina -d oficina"]
      interval: 5s
      timeout: 3s
      retries: 10

volumes:
  oficina_pgdata:
```

Suba somente o banco:

```bash
docker compose up -d postgres
```

Veja os contêineres:

```bash
docker compose ps
```

Acompanhe os logs:

```bash
docker compose logs -f postgres
```

Teste a disponibilidade:

```bash
docker compose exec postgres \
  psql -U oficina -d oficina -c "SELECT current_database(), current_user;"
```

Como a aplicação está fora do Docker, use:

```text
jdbc:postgresql://localhost:5432/oficina
```

Depois execute:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 12. Dockerfile da aplicação

O Dockerfile pressupõe que o JAR já tenha sido gerado:

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/oficina-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Gerar o JAR antes da imagem

```bash
./mvnw clean package
```

### Construir a imagem

```bash
docker build -t oficina-api:local .
```

### Listar imagens

```bash
docker image ls oficina-api
```

### Executar a imagem isoladamente

Se o PostgreSQL estiver instalado na máquina host, o endereço do banco depende do sistema e da configuração da rede Docker. Em Docker Desktop, normalmente pode ser usado `host.docker.internal`:

```bash
docker run --rm \
  --name oficina-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/oficina \
  -e DB_USERNAME=oficina \
  -e DB_PASSWORD='senha-segura' \
  -e JWT_SECRET='CHAVE_BASE64' \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD='senha-administrativa-segura' \
  oficina-api:local
```

No Linux, pode ser necessário adicionar:

```bash
--add-host=host.docker.internal:host-gateway
```

Para banco e aplicação em contêineres, prefira Docker Compose.

---

## 13. Subir aplicação e PostgreSQL com Docker Compose

Exemplo completo:

```yaml
services:
  postgres:
    image: postgres:17-alpine
    container_name: oficina-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-oficina}
      POSTGRES_USER: ${POSTGRES_USER:-oficina}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - oficina_pgdata:/var/lib/postgresql/data
    healthcheck:
      test:
        - CMD-SHELL
        - pg_isready -U ${POSTGRES_USER:-oficina} -d ${POSTGRES_DB:-oficina}
      interval: 5s
      timeout: 3s
      retries: 10
      start_period: 10s
    restart: unless-stopped

  api:
    build:
      context: .
      dockerfile: Dockerfile
    image: oficina-api:${APP_VERSION:-local}
    container_name: oficina-api
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-oficina}
      DB_USERNAME: ${POSTGRES_USER:-oficina}
      DB_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      ADMIN_USERNAME: ${ADMIN_USERNAME}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      SERVER_PORT: 8080
      OPENAPI_ENABLED: ${OPENAPI_ENABLED:-false}
      SWAGGER_UI_ENABLED: ${SWAGGER_UI_ENABLED:-false}
    ports:
      - "${APP_PORT:-8080}:8080"
    restart: unless-stopped

volumes:
  oficina_pgdata:
```

Dentro da rede do Docker Compose, o hostname do banco é o nome do serviço:

```text
postgres
```

Por isso a URL é:

```text
jdbc:postgresql://postgres:5432/oficina
```

Não use `localhost` para conectar a API ao banco quando ambos estão em contêineres separados.

---

## 14. Arquivo `.env` para Docker Compose

Copie o exemplo:

```bash
cp .env.example .env
```

Exemplo de `.env` local:

```dotenv
POSTGRES_DB=oficina
POSTGRES_USER=oficina
POSTGRES_PASSWORD=troque-esta-senha

JWT_SECRET=CHAVE_JWT_EM_BASE64
ADMIN_USERNAME=admin
ADMIN_PASSWORD=troque-esta-senha-tambem

APP_VERSION=local
APP_PORT=8080
OPENAPI_ENABLED=true
SWAGGER_UI_ENABLED=true
```

Gere uma chave JWT:

```bash
openssl rand -base64 32
```

Regras importantes:

- Nunca versionar o arquivo `.env`.
- Versionar somente `.env.example`, sem segredos reais.
- Em produção, usar o mecanismo de secrets da plataforma.
- Não incluir valores sensíveis diretamente em `application-prod.yml` ou `compose.yml`.

Garanta que o `.gitignore` contenha:

```gitignore
.env
```

---

## 15. Construir e subir com Docker Compose

O Dockerfile atual copia um JAR já existente. Portanto, primeiro gere o artefato:

```bash
./mvnw clean package
```

Depois construa as imagens:

```bash
docker compose build
```

Suba o ambiente:

```bash
docker compose up -d
```

Construir e subir em um único comando:

```bash
docker compose up -d --build
```

Forçar reconstrução sem cache:

```bash
docker compose build --no-cache api
docker compose up -d
```

Ver o estado:

```bash
docker compose ps
```

Acompanhar os logs da API:

```bash
docker compose logs -f api
```

Acompanhar todos os logs:

```bash
docker compose logs -f
```

Reiniciar somente a API:

```bash
docker compose restart api
```

Parar os serviços sem remover os contêineres:

```bash
docker compose stop
```

Parar e remover os contêineres:

```bash
docker compose down
```

Parar e remover também o volume do PostgreSQL:

```bash
docker compose down -v
```

> O uso de `down -v` apaga os dados persistidos no volume. Use somente quando quiser recriar o banco do zero.

---

## 16. Executar no profile `prod`

O profile `prod` não deve possuir valores padrão para credenciais ou segredos.

### Executar JAR localmente

Linux ou macOS:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/oficina
export DB_USERNAME=oficina
export DB_PASSWORD='senha-segura'
export JWT_SECRET="$(openssl rand -base64 32)"
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD='senha-administrativa-segura'

java -jar target/oficina-api-0.0.1-SNAPSHOT.jar
```

### Executar com argumentos

```bash
java -jar target/oficina-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Executar pelo Docker Compose

O serviço `api` deve declarar:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
```

Depois:

```bash
docker compose up -d --build
```

---

## 17. Confirmar o profile ativo

Durante a inicialização, procure no log uma mensagem semelhante a:

```text
The following 1 profile is active: "dev"
```

ou:

```text
The following 1 profile is active: "prod"
```

Também é possível executar com `--debug` para obter o relatório de condições da auto-configuração:

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.arguments=--debug
```

Não utilize `--debug` permanentemente em produção.

---

## 18. Verificar as migrations do Flyway

Ao iniciar, os logs devem indicar:

- conexão com o banco;
- validação das migrations;
- criação de `flyway_schema_history`, quando necessária;
- execução das versões ainda não aplicadas;
- validação do schema pelo Hibernate.

Liste as tabelas:

```bash
docker compose exec postgres psql -U oficina -d oficina -c "\dt"
```

Consulte o histórico do Flyway:

```bash
docker compose exec postgres psql -U oficina -d oficina -c \
  "SELECT installed_rank, version, description, script, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Tabelas esperadas inicialmente:

```text
flyway_schema_history
ordem_servico
```

### Adicionar uma nova migration

Nunca altere uma migration já aplicada em um ambiente compartilhado. Crie uma nova versão:

    V2__create_cliente.sql
    V3__create_veiculo.sql
    V4__add_cliente_fk_to_ordem_servico.sql

Depois reinicie a aplicação. O Flyway aplicará apenas as versões pendentes.

---

## 19. Acessar a aplicação

API:

```text
http://localhost:8080
```

Swagger, quando habilitado:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Em produção, Swagger e OpenAPI devem permanecer desabilitados por padrão, salvo decisão consciente da equipe e proteção adequada do acesso.

---

## 20. Autenticação local

Solicite um token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Use o token retornado:

```bash
curl http://localhost:8080/api/v1/ordens-servico/1 \
  -H "Authorization: Bearer SEU_TOKEN"
```

As credenciais acima são somente um exemplo de desenvolvimento. Não use senhas padrão em produção.

---

## 21. Diagnóstico de problemas

### Erro: `Schema validation: missing table [ordem_servico]`

Verifique:

1. A dependência `spring-boot-starter-flyway` está no `pom.xml`.
2. `flyway-database-postgresql` também está presente.
3. A migration está em `src/main/resources/db/migration`.
4. O nome contém dois underscores, como `V1__create_ordem_servico.sql`.
5. A aplicação está conectando ao banco correto.
6. O usuário possui permissão para criar tabelas no schema.
7. O log contém mensagens do Flyway antes da inicialização do Hibernate.
8. A tabela `flyway_schema_history` existe.

Limpe e execute novamente:

```bash
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
```

### Porta `5432` já está em uso

Verifique o processo:

```bash
lsof -i :5432
```

Ou altere a porta publicada no Compose:

```yaml
ports:
  - "5433:5432"
```

Nesse caso, a aplicação executada fora do Docker usa:

```text
jdbc:postgresql://localhost:5433/oficina
```

A aplicação executada dentro do mesmo Compose continua usando:

```text
jdbc:postgresql://postgres:5432/oficina
```

### Porta `8080` já está em uso

Execute localmente em outra porta:

```bash
java -jar target/oficina-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev \
  --server.port=8081
```

Ou altere no `.env`:

```dotenv
APP_PORT=8081
```

### Erro de autenticação no PostgreSQL

Teste diretamente:

```bash
psql -h localhost -p 5432 -U oficina -d oficina
```

Confira:

- usuário;
- senha;
- nome do banco;
- host e porta;
- variáveis de ambiente já definidas;
- volume antigo do Docker com credenciais anteriores.

A imagem oficial do PostgreSQL utiliza as variáveis de inicialização apenas quando o diretório de dados está vazio. Em ambiente descartável de desenvolvimento, pode ser necessário recriar o volume:

```bash
docker compose down -v
docker compose up -d postgres
```

Isso remove todos os dados do banco desse volume.

### API em Docker não conecta ao PostgreSQL

Dentro do Compose, use:

```text
jdbc:postgresql://postgres:5432/oficina
```

Não use:

```text
jdbc:postgresql://localhost:5432/oficina
```

Dentro do contêiner da API, `localhost` representa o próprio contêiner da API, não o contêiner do PostgreSQL.

---

## 22. Fluxos recomendados

### Desenvolvimento diário com banco em Docker

```bash
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Validação local completa

```bash
./mvnw clean verify
./mvnw clean package
docker compose up -d --build
docker compose logs -f api
```

### Ambiente de produção

1. Gerar o JAR em pipeline.
2. Executar testes e análise de segurança.
3. Construir uma imagem versionada e imutável.
4. Publicar a imagem em um registry.
5. Injetar segredos pela plataforma de implantação.
6. Iniciar com `SPRING_PROFILES_ACTIVE=prod`.
7. Manter `ddl-auto=validate` e aplicar migrations pelo Flyway.
8. Manter Swagger desabilitado, salvo necessidade controlada.

---

## 23. Checklist antes de enviar alterações

- [ ] A aplicação compila com Java 21.
- [ ] Os testes passam.
- [ ] A cobertura foi gerada.
- [ ] As migrations executam em um banco vazio.
- [ ] O Hibernate valida o schema sem erros.
- [ ] O profile `dev` executa localmente.
- [ ] A imagem Docker é construída.
- [ ] O Docker Compose sobe API e PostgreSQL.
- [ ] Nenhum segredo foi commitado.
- [ ] `.env` está no `.gitignore`.
- [ ] `.env.example` está atualizado.
- [ ] O README e esta documentação refletem os comandos atuais.

---

## 24. Comandos rápidos

Subir somente o banco:

```bash
docker compose up -d postgres
```

Executar aplicação em desenvolvimento:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Testar e gerar cobertura:

```bash
./mvnw clean verify
```

Empacotar:

```bash
./mvnw clean package
```

Construir imagem:

```bash
docker build -t oficina-api:local .
```

Subir tudo:

```bash
docker compose up -d --build
```

Acompanhar logs:

```bash
docker compose logs -f api
```

Encerrar ambiente:

```bash
docker compose down
```

