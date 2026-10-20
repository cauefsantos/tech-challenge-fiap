<!-- Extraido do banco do EverCode, sessao ses_zffe5eaa20b04fffevUgQaX4QgX847o, mensagem de 2026-10-19 17:37. Texto original sem alteracoes. Versao revisada com o Miro: 02-plano-fase1-revisado.md -->

# Plano de cumprimento da Fase 1 (MVP)

Li os documentos (`docs/Fase 1.md`, o checklist, `ENTIDADES_FASE1.md`, a documentação DDD e os READMEs) e o código. Também rodei `mvnw clean verify` com JDK 21 para medir o estado real.

## 1. A estrutura atual atende?

**Arquitetura: sim, no esqueleto.** O monólito está em camadas (`domain`, `application`, `infrastructure`, `presentation`), com repositórios como portas no domínio e adapters JPA na infraestrutura. Isso atende "monolito em camadas" e "DDD". Também existem Flyway com migrations para H2 e PostgreSQL, JWT, Swagger (springdoc), JaCoCo e o profile `prod` com segredos vindos de variáveis de ambiente.

**Fluxos e entregáveis: não.** A estrutura comporta o que falta, mas o funcional e os entregáveis estão incompletos:

| Exigência | Estado | Evidência |
|---|---|---|
| CRUD cliente, veículo, serviço, material | **Atende** | 4 controllers com POST, GET, PUT, DELETE e PATCH `/ativacao` |
| Validação CPF/CNPJ e placa | **Atende** | `Cliente.java:51-84`, `Veiculo.java:26-29` (sem value objects, ver Fase 5) |
| Regras de domínio (transições, orçamento, estoque) | **Atende no domínio** | `OrdemServico.java`, `Orcamento.java`, `Material.java` |
| **Fluxo da OS via API** | **Não atende** | `OrdemServicoController` só tem criar e buscar por id |
| **Persistência da OS completa** | **Não atende (bug)** | `OrdemServicoRepositoryAdapter.toEntity` salva só o cabeçalho. Itens, orçamentos e histórico nunca são gravados nem lidos, embora as tabelas existam na migration |
| Estoque | **Parcial** | `Material.consumir()` existe, mas nenhum use case o chama. `ItemMaterial` só valida e não baixa |
| Listagem de OS | **Não atende** | O repositório não tem `listar` |
| Consulta de status pelo cliente | **Não atende** | Só existe o GET autenticado, e não há como o cliente consultar por CPF ou número |
| Tempo médio de execução | **Não atende** | Existem `dataInicioExecucao` e `dataFinalizacao`, mas nenhuma consulta ou endpoint |
| JWT | **Atende, com ressalva** | Existe login. `application.yml:10-14` traz segredo JWT e senha `admin123` como default, o que viola "sem credenciais" |
| Testes | **Insuficiente** | 15 testes passando, **cobertura total de 39,8%** (instruções). Domínio da OS 62%, `Orcamento` 54%, `Cliente` 46%, controllers e adapters quase 0%. Não há testes de integração de fluxo |
| Dockerfile, compose, `.env.example` | **Ausentes** | Verificado: os três não existem |
| README | **Quebrado** | Fala em `dotnet test`, Swagger em `:5000/:5001/swagger`, Java 17, "Descrever os motivos", estrutura `Api/Tests` e RMs duplicados. Falta justificativa do banco, credenciais de demo e links do vídeo e do DDD |
| Docs DDD | **Parcial** | Há `docs/ddd/linguagem-ubiqua.md` e `Documentacao_DDD_Fase1.md`, que cita "Peça" e `Domain/Entities`, divergindo do código (`Material`, `domain/<ctx>/model`). Faltam `event-storming.pdf` e `diagramas.pdf`. O PDF do Miro está em `.dev/` |
| Análise de vulnerabilidades | **Ausente** | Sem relatório e sem plugin |
| CI/branch protection | **Não verificável** | Não há `.github/`. A proteção de `main` é configuração no GitHub |

**Dois achados de ambiente:**
- O `JAVA_HOME` padrão desta máquina é Java 8, e o build falha com erros enganosos de `record`. Com JDK 21 o build passa. O README precisa dizer "Java 21 obrigatório", porque o `pom.xml` fixa 21 e o README diz "17 ou 21".
- O `.gitignore` não contém `.env`.

## 2. Plano de execução

A ordem respeita as dependências. A Fase 1 destrava as demais.

### Fase 0: Pré-requisitos (0,5 dia)
1. Definir o fluxo de branches: `develop` → PR → `main`, e habilitar proteção de `main` no GitHub (ação manual de vocês).
2. Acrescentar `.env` ao `.gitignore`.
3. Remover os defaults sensíveis de `application.yml` (JWT secret e `admin123`). O profile `dev` passa a ler de variável de ambiente ou de um `.env` local.

### Fase 1: Persistência completa do agregado OS (bloqueante, 1 a 1,5 dia)
1. Criar as JPA entities de `ItemServico`, `ItemMaterial`, `Orcamento` e `HistoricoStatus`. `Orcamento` já tem entity em `infrastructure/orcamento`, mas sem adapter.
2. Fazer o `OrdemServicoRepositoryAdapter` gravar e ler o agregado inteiro, com mapeamento nos dois sentidos.
3. Acrescentar `listar` e filtros (`status`, `clienteId`) ao `OrdemServicoRepository`.
4. Validar com um teste de integração (round-trip salvar → buscar com itens, orçamento e histórico).

### Fase 2: Casos de uso e endpoints do fluxo da OS (2 dias)
Novos use cases, cada um com `@Transactional`:
- Enviar para diagnóstico e registrar diagnóstico.
- Adicionar e remover item de serviço (valida serviço ativo, snapshot de preço e tempo).
- Adicionar e remover item de material (valida disponibilidade).
- Gerar orçamento e disponibilizar orçamento.
- Aprovar e rejeitar orçamento. A rejeição precisa de regra para a OS (hoje `Orcamento.rejeitar` existe, mas a OS não reage).
- Finalizar e entregar.
- **Baixa de estoque** ao iniciar a execução (ou ao aprovar, a decidir). Ela deve estar na mesma transação.

Endpoints sugeridos em `/api/v1/ordens-servico`:
- `GET` (lista paginada) e `GET /{id}`.
- `POST /{id}/diagnostico`, `/itens-servico`, `/itens-material`.
- `POST /{id}/orcamentos`, `/orcamentos/{oid}/disponibilizar`, `/aprovar`, `/rejeitar`.
- `POST /{id}/execucao`, `/finalizacao`, `/entrega`.

**Decisão necessária:** quem aprova o orçamento, o admin ou o cliente? O desafio fala em "autorizar via aplicativo". Isso afeta a Fase 3.

### Fase 3: Consulta pública e métricas (1 dia)
1. **Consulta do cliente:** `GET /api/v1/public/ordens-servico/{numero}`, aberto no `SecurityConfiguration`, mas com a identificação do cliente (CPF ou CNPJ) como parâmetro obrigatório. O DTO deve expor só status, histórico e valor, nunca dados pessoais. Aplicar rate limit simples se houver tempo.
2. **Tempo médio de execução:** `GET /api/v1/ordens-servico/metricas/tempo-medio-execucao`, calculado por `dataFinalizacao − dataInicioExecucao` das OS finalizadas, com o resultado "sem amostras" tratado explicitamente.
3. **Estoque:** endpoint de entrada de estoque e listagem de materiais abaixo do mínimo.

### Fase 4: Infraestrutura (1 dia)
1. `Dockerfile` multi-stage (build com JDK 21 e runtime JRE 21), executando como usuário não root. O `.dev/README.md` já descreve um que depende de JAR pré-gerado, e o multi-stage evita esse passo.
2. `compose.yml` com Postgres 17 (healthcheck) e API no profile `prod`.
3. `.env.example` sem segredos reais.
4. Validar com `docker compose up --build`.

### Fase 5: Testes e cobertura ≥ 80% nos domínios críticos (2 dias)
1. **Unitários do domínio:** completar `OrdemServico` (todas as transições inválidas), `Orcamento`, `Cliente` (CPF/CNPJ válidos e inválidos), `Veiculo`, `Material` e `Servico`.
2. **Use cases:** testes com Mockito para os novos fluxos.
3. **Integração:** `@SpringBootTest` com MockMvc cobrindo o fluxo completo (login → cliente → veículo → OS → itens → orçamento → aprovação → execução → finalização → entrega), 401/403 sem token, e validação de CPF e placa inválidos.
4. **Transacionalidade:** teste de que falha no meio do fluxo não deixa estoque baixado.
5. Configurar `jacoco:check` com gate de 80% por bundle nos pacotes `domain.*` e `application.*`, e excluir DTOs e entities do cálculo.
6. Opcional: introduzir os value objects `Documento` e `Placa`, como o `ENTIDADES_FASE1.md` recomenda. É refactor de baixo risco, mas só vale depois dos testes verdes.

### Fase 6: Segurança (0,5 dia)
1. Adicionar o OWASP Dependency-Check (`dependency-check-maven`) e gerar o relatório em `docs/security/`. O README já cita essa ferramenta.
2. Revisar o JWT: validar `issuer` no decoder, garantir que o secret tenha ≥ 32 bytes, e confirmar que o profile `prod` não expõe Swagger.
3. Registrar o resultado e a decisão sobre os achados.

### Fase 7: Documentação e entregáveis (1,5 dia)
1. **README:** reescrever com todos os itens exigidos. Remover as referências a .NET, corrigir o Swagger para `:8080/swagger-ui.html`, e acrescentar a justificativa do PostgreSQL (e do H2 como conveniência de dev), as credenciais de demo via variável de ambiente, a estrutura real de diretórios, os comandos de teste e cobertura, e os links do DDD e do vídeo.
2. **Docs DDD:** alinhar `Documentacao_DDD_Fase1.md` ao código (`Material` com `PECA`/`INSUMO`, `Orcamento`, `HistoricoStatus`, os eventos que realmente existem). Mover e exportar o Miro para `docs/ddd/event-storming.pdf` e `diagramas.pdf`. Incluir uma tabela "conceito DDD → classe no código".
3. **OpenAPI:** adicionar `@Tag` e `@Operation` e exportar `api-docs` para `docs/`.
4. **Evidências:** captura do JaCoCo, relatório de vulnerabilidades, Swagger e fluxo JWT.
5. **Vídeo (≤ 15 min, YouTube público):** roteiro seguindo o checklist da seção 5.
6. **PDF final:** identificação, repositório, branch e hash do commit final, vídeo e link do Miro. Corrigir os RMs duplicados na tabela de integrantes.

## 3. Cronograma e riscos

Estimativa total de 9 a 10 dias-pessoa. Com 5 integrantes, dá para paralelizar:

| Trilha | Fases |
|---|---|
| A (núcleo) | 1 → 2 → 3 |
| B (infra e segurança) | 0 → 4 → 6 |
| C (qualidade) | 5, depois de A estabilizar |
| D (docs e entrega) | 7 |

**Riscos principais:**
1. **A Fase 1 é o caminho crítico.** Sem persistir o agregado, nada do fluxo da OS funciona, e o vídeo não consegue demonstrar orçamento nem status.
2. **A cobertura de 80%** está hoje 40 pontos abaixo. É a principal ameaça à nota.
3. **Segredos versionados:** o `application.yml` atual já contém um segredo JWT e uma senha de admin. Mesmo sendo valores de dev, o enunciado proíbe credenciais no repositório. Se o histórico git for avaliado, o commit antigo continua visível, então decidam se vale reescrever o histórico.
4. **Divergência código × documentação:** o enunciado diz que documentação que não corresponde ao código não conta como evidência.

## 4. Pontos que dependem de vocês
1. Quem aprova o orçamento: admin autenticado ou cliente via endpoint público? R.:Por enquanto, somente admin autenticado
2. Em que momento baixar o estoque: na aprovação, no início da execução ou na inclusão do item? R.: Na aprovação do orçamento, para garantir que o material esteja disponível antes de iniciar a execução.
3. Reescrever o histórico git para remover o segredo antigo, ou apenas rotacionar e documentar? R.: Rotacionar e documentar, para evitar problemas com histórico e manter a integridade do repositório.

## Observação sobre este diagnóstico
A cobertura e os testes foram medidos nesta sessão com JDK 21. Não executei a aplicação nem o Docker. As lacunas de endpoint e de persistência vêm da leitura do código e foram confirmadas pela listagem de arquivos e pelas anotações de mapeamento. O `Miro - Fase 01.pdf` não foi lido, então não confrontei o conteúdo dele com a documentação em `docs/`.

Posso gravar este plano em `.evercode/plans/` e começar pela Fase 1, que é o caminho crítico. Ou posso responder antes as três decisões acima. Qual você prefere?
