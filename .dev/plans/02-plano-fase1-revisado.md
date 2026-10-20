# Plano revisado de cumprimento da Fase 1 (MVP), com ajustes do Miro

Base: plano original (`00-plano-fase1-original-extraido.md`) + análise do Miro (`01-analise-miro-fase01.md`).
Pacote base: `br.com.fiap.techchallengefiap`. Caminhos abaixo relativos a `src/main/java/<pacote>/` salvo indicação.

## 0. Como ler este plano

- **Decisões assumidas:** onde o Miro deixa dúvida, adotei a opção marcada como "recomendada" na análise (D-A a D-K). Cada item que depende delas traz a tag `[D-x]`. Se uma decisão mudar, só os itens com a tag mudam.
- **Limite herdado do Miro:** págs. 5 e 6 (hotspots) e o texto exato de D06 não foram lidos. O plano não depende deles, mas podem existir regras ali que ainda não estão cobertas.
- **Build:** usar JDK 21 (`$env:JAVA_HOME="C:\Users\gabriel.costa\JAVAS\jdk-21.0.9+10"`). O padrão da máquina é Java 8 e gera erros enganosos de `record`.
- **Baseline medido em 2026-10-19:** 15 testes passando, cobertura total de instruções 39,8%.

## 1. O que o Miro mudou no plano original

| Mudança | Efeito no plano |
|---|---|
| Novo status **CANCELADA** por rejeição (D05) | Nova fase 2.1 (domínio) e testes |
| Estoque baixado **na aprovação**, que **depende de estoque suficiente** (D07) | Responde a decisão 2 do plano original: baixar na aprovação, não no início da execução nem na inclusão |
| Itens só após o diagnóstico (D02, D03) | Nova regra de domínio na 2.1 |
| Passo "Concluir diagnóstico" | `concluirDiagnostico()` na 2.1 `[D-A]` |
| Estoque insuficiente sem tratamento definido | Bloquear a aprovação e manter a OS aguardando `[D-B]` |
| Dependência circular `orcamento` ↔ `ordemservico` | Item 2.2 (refatoração) |
| Eventos e BCs do Miro diferem da doc DDD | Nova Fase 7 de ajuste da doc DDD |
| Sem papéis de funcionário (D06) | Não criar entidade Funcionário. JWT basta |

## Fase 0. Pré-requisitos (0,5 dia)

**0.1 Fluxo de branches.** Manual no GitHub: `develop` → PR → `main`, com proteção de `main` (revisão obrigatória, sem push direto). Não verificável pelo código.

**0.2 `.gitignore`.** Acrescentar `.env`, `.env.*` e a exceção `!.env.example`.

**0.3 Segredos em `src/main/resources/application.yml:10-14`.** Remover o default do segredo JWT e da senha `admin123`. Ler de `${JWT_SECRET}` e `${ADMIN_PASSWORD}` sem default. O profile `dev` (`application-dev.yml`) lê as mesmas variáveis. Ajustar `infrastructure/security/SecurityProperties.java` e `AdminProperties.java` para falhar na partida se vazias (`@NotBlank` + `@Validated`). Verificação: subir sem as variáveis e confirmar falha explícita.

**0.4 Histórico git.** Decisão do grupo: rotacionar o segredo e documentar, ou reescrever o histórico. O segredo antigo permanece visível nos commits.

## Fase 1. Persistência completa do agregado OS (1 a 1,5 dia, caminho crítico)

Hoje `infrastructure/ordemservico/OrdemServicoRepositoryAdapter.toEntity` grava só o cabeçalho. As tabelas `item_servico`, `item_material`, `orcamento` e `historico_status` já existem na migration (`db/migration/postgresql/V1__create_ordem_servico.sql`, versão H2 equivalente).

**1.1 Entities JPA.** Já existem `ItemServicoJpaEntity`, `ItemMaterialJpaEntity`, `HistoricoStatusJpaEntity` em `infrastructure/ordemservico/` e `OrcamentoJpaEntity` em `infrastructure/orcamento/`. Acrescentar em `OrdemServicoJpaEntity` as coleções `@OneToMany(mappedBy = "ordemServico", cascade = ALL, orphanRemoval = true)` para os quatro filhos. Conferir os mapeamentos de coluna contra o SQL (`numeric(15,3)` em quantidade, `numeric(15,2)` em valores).

**1.2 Adapter nos dois sentidos.** Em `OrdemServicoRepositoryAdapter`: `toEntity` mapeia cabeçalho, itens, orçamentos e histórico. `toDomain` reconstrói o agregado usando os construtores de reconstituição (`Orcamento` já tem o construtor com id, linha 36). Separar em métodos pequenos (`mapearItensServico`, `mapearItensMaterial`, `mapearOrcamentos`, `mapearHistorico`) para respeitar o limite de ~30 linhas.

**1.3 Repositório.** Em `domain/ordemservico/repository/OrdemServicoRepository.java` (hoje só `salvar` e `buscarPorId`) acrescentar `Page<OrdemServico> listar(StatusOrdemServico status, Long clienteId, Pageable pageable)` e `List<OrdemServico> listarFinalizadas()` para a métrica. O domínio não deve depender de Spring Data: usar um tipo próprio de paginação em `domain/shared` ou retornar `List` com limite, seguindo o que os outros repositórios do projeto fazem (verificar `ClienteRepository` antes de decidir).

**1.4 Concorrência de estoque.** `MaterialJpaEntity` não tem `@Version` (verificado por busca). Acrescentar `@Version Long versao` e a coluna correspondente em **nova migration** `V2__...` para H2 e PostgreSQL. Sem isso, duas aprovações simultâneas podem baixar o mesmo estoque duas vezes.

**1.5 Teste de integração.** `@DataJpaTest`/`@SpringBootTest` com round-trip: salvar OS com 2 itens de serviço, 2 de material, 1 orçamento e histórico, buscar e comparar todos os campos.

Critério de aceite: o round-trip passa e o `GET /{id}` devolve itens, orçamento e histórico.

## Fase 2. Domínio e casos de uso do fluxo da OS (2 a 3 dias)

### 2.1 Ajustes de domínio (alinhados ao Miro)

Arquivo: `domain/ordemservico/model/OrdemServico.java` e `StatusOrdemServico.java`.

1. **`StatusOrdemServico`:** acrescentar `CANCELADA` como terminal `[D-C]`. Sem migration de constraint (a coluna não tem CHECK, `V1__create_ordem_servico.sql:57`).
2. **`rejeitarOrcamento(Long orcamentoId, String observacao)`:** exige status AGUARDANDO_APROVACAO. Chama `Orcamento.rejeitar(observacao)` (já existe, linha 62), faz `transicionar(AGUARDANDO_APROVACAO, CANCELADA)` e registra o histórico. Integral, sem rejeição parcial (D04).
3. **`concluirDiagnostico()`** `[D-A]`: exige EM_DIAGNOSTICO e diagnóstico preenchido. Hoje `registrarDiagnostico` cobre o texto. Se `concluirDiagnostico` não mudar de status (o Miro não tem status entre diagnóstico e aguardando), ele apenas marca o diagnóstico como concluído e libera `gerarOrcamento`. Decidir com o grupo se vale um campo `diagnosticoConcluido` ou se o passo fica implícito em `gerarOrcamento`.
4. **Itens só em EM_DIAGNOSTICO** `[D-G]`: em `adicionarItemServico` e `adicionarItemMaterial` (linhas 84-98) exigir `status == EM_DIAGNOSTICO`. Hoje só `verificarEdicao()` bloqueia após aprovação ou entrega.
5. **Finalizar exige ao menos um ItemServico** `[D-H]`: em `finalizar()`. Regra já citada em `Documentacao_DDD_Fase1.md` §10.
6. **Estoque na aprovação:** o domínio da OS não acessa o repositório. O caso de uso (2.3) verifica e baixa. Acrescentar em `OrdemServico` um método de consulta `List<ItemMaterial> getItensMaterial()` já usado pelo caso de uso, sem lógica nova.
7. **Início automático da execução** `[D-D]`: manter `aprovarOrcamento()` chamando `iniciarExecucao()`. Documentar.

Cada regra nova gera teste unitário positivo e negativo (ver Fase 5).

### 2.2 Dependência circular entre pacotes `[D-F]`

Verificado: `Orcamento.java:3-4` importa `ordemservico.model.ItemMaterial/ItemServico`, e `OrdemServico.java:4-5` importa `orcamento.model.Orcamento/StatusOrcamento`.

- **Opção recomendada:** mover `domain/orcamento/model/*` para `domain/ordemservico/model/` e `infrastructure/orcamento/OrcamentoJpaEntity` para `infrastructure/ordemservico/`. Orçamento é entidade do agregado OS no Miro (pág. 11) e do BC Ordem de Serviço (pág. 13).
- Refatoração mecânica (mover + ajustar imports). Fazer em commit isolado, **antes** das demais mudanças da Fase 2, com o build verde antes e depois.
- **Exige aprovação do grupo.** Se negada, documentar a dependência circular como débito conhecido.

### 2.3 Casos de uso (`application/ordemservico/`)

Padrão existente: um use case por classe, comando como record (`CriarOrdemServicoCommand`). Todos `@Transactional`. Novos:

| Use case | Regra principal |
|---|---|
| `IniciarDiagnosticoUseCase` | `enviarParaDiagnostico()` |
| `RegistrarDiagnosticoUseCase` | `registrarDiagnostico(texto)` e `concluirDiagnostico()` |
| `AdicionarItemServicoUseCase` / `RemoverItemServicoUseCase` | valida serviço ativo, congela preço e tempo (snapshot) |
| `AdicionarItemMaterialUseCase` / `RemoverItemMaterialUseCase` | valida material ativo. **Não baixa estoque**, no máximo valida disponibilidade como aviso (D07) |
| `GerarOrcamentoUseCase` / `DisponibilizarOrcamentoUseCase` | `gerarOrcamento()`, `disponibilizarOrcamento()` |
| `AprovarOrcamentoUseCase` | ver abaixo |
| `RejeitarOrcamentoUseCase` | `rejeitarOrcamento()` → CANCELADA |
| `FinalizarOrdemServicoUseCase` / `EntregarOrdemServicoUseCase` | `finalizar()`, `entregar()` |
| `ListarOrdensServicoUseCase` | filtros `status`, `clienteId` |

**`AprovarOrcamentoUseCase` (regra central, D07):**
1. Buscar OS.
2. Para cada item de material, buscar o `Material` e chamar `validarDisponibilidade(quantidade)`. Se algum falhar, lançar `DomainException` com a lista dos itens insuficientes (`[D-B]`: a OS permanece AGUARDANDO_APROVACAO, nada é alterado).
3. Só se todos passarem: `os.aprovarOrcamento(orcamentoId, observacao)`.
4. Para cada item, `material.consumir(quantidade)` e salvar.
5. Salvar a OS.
Tudo na mesma transação, para que uma falha no passo 4 ou 5 reverta o estoque. A versão otimista (1.4) protege contra aprovações simultâneas.

### 2.4 Endpoints (`presentation/rest/ordemservico/`)

Hoje o controller tem só criar e buscar por id. Acrescentar, em `/api/v1/ordens-servico` (conferir o prefixo atual antes), com request/response records e validação (`@Valid`):

| Método e caminho | Use case |
|---|---|
| `GET /` (paginado, `?status=&clienteId=`) | Listar |
| `GET /{id}` | Buscar (já existe) |
| `POST /{id}/diagnostico` e `POST /{id}/diagnostico/conclusao` | Iniciar/registrar/concluir |
| `POST /{id}/itens-servico`, `DELETE /{id}/itens-servico/{itemId}` | Itens de serviço |
| `POST /{id}/itens-material`, `DELETE /{id}/itens-material/{itemId}` | Itens de material |
| `POST /{id}/orcamentos` | Gerar |
| `POST /{id}/orcamentos/{oid}/disponibilizar` | Disponibilizar |
| `POST /{id}/orcamentos/{oid}/aprovar` | Aprovar (estoque + execução) |
| `POST /{id}/orcamentos/{oid}/rejeitar` | Rejeitar (cancela a OS) |
| `POST /{id}/finalizacao`, `POST /{id}/entrega` | Finalizar e entregar |

`GlobalExceptionHandler` já mapeia `DomainException`, `ConflictException` e `ResourceNotFoundException`. Conferir que erro de estoque insuficiente retorna 409 (ou 422) com mensagem útil, sem expor detalhes internos.

**Decisão pendente herdada do plano original:** quem aprova, o funcionário autenticado ou o cliente por endpoint público? O Miro (pág. 1) diz que o cliente decide, e D06 diz que não há papéis de funcionário. Recomendação para o MVP: o funcionário registra a decisão do cliente nos endpoints autenticados acima. Isso evita um fluxo público de escrita no MVP.

## Fase 3. Consulta pública e métricas (1 dia)

**3.1 Consulta do cliente.** `GET /api/v1/public/ordens-servico/{numero}?documento=...`, liberado em `SecurityConfiguration` só para essa rota e só `GET`. Exigir o documento do cliente e comparar com o da OS. Resposta mínima (status, histórico de status, valor), sem dados pessoais. Resposta idêntica para "não existe" e "documento não confere", para não permitir enumeração. Rate limit por IP, se houver tempo.

**3.2 Tempo médio de execução.** `GET /api/v1/ordens-servico/metricas/tempo-medio-execucao`: média de `dataFinalizacao − dataInicioExecucao` das OS FINALIZADAS e ENTREGUES. Com zero amostras retornar `amostras: 0` e `tempoMedioSegundos: null`, nunca 0.

**3.3 Estoque.** Endpoint de entrada de estoque (Miro, pág. 15: "Atualizar quantidade em estoque") e listagem de materiais abaixo do mínimo.

## Fase 4. Infraestrutura (1 dia)

1. `Dockerfile` multi-stage: build com JDK 21, runtime JRE 21, usuário não root.
2. `compose.yml` com PostgreSQL (healthcheck) e API no profile `prod`.
3. `.env.example` com nomes de variável e valores de exemplo claramente falsos.
4. Validar com `docker compose up --build` e `GET` no Swagger. **Não executado ainda**: o Docker não foi testado em nenhuma sessão.

## Fase 5. Testes e cobertura (2 a 3 dias)

Meta: 80% nos pacotes `domain.*` e `application.*` (medida hoje: 39,8% no total, domínio da OS 62%, `Orcamento` 54%, `Cliente` 46%, controllers e adapters ~0%).

1. **Domínio:** todas as transições válidas e inválidas da OS, incluindo as novas: CANCELADA por rejeição, bloqueio de itens fora de EM_DIAGNOSTICO, finalização sem item de serviço, rejeição fora de AGUARDANDO_APROVACAO, aprovação duplicada. `Orcamento`, `Cliente` (CPF/CNPJ válidos e inválidos), `Veiculo` (placa antiga e Mercosul), `Material`, `Servico`.
2. **Use cases (Mockito):** `AprovarOrcamentoUseCase` com estoque suficiente, insuficiente (nada baixado, OS inalterada) e falha no meio (rollback).
3. **Integração (`@SpringBootTest` + MockMvc):** fluxo completo login → cliente → veículo → serviço → material → OS → diagnóstico → itens → orçamento → aprovação → finalização → entrega. Fluxo alternativo: rejeição → CANCELADA e estoque intacto. 401 sem token. CPF e placa inválidos.
4. **Transacionalidade:** teste real com banco (H2) comprovando que estoque e OS não ficam inconsistentes quando o passo final falha.
5. **JaCoCo:** `jacoco:check` com 80% de instruções por bundle em `domain.*` e `application.*`, excluindo DTOs e entities JPA.
6. **Value objects** (`Documento`, `Placa`, `Dinheiro`, `Quantidade`, conforme Miro pág. 10): opcional, só depois dos testes verdes e com aprovação.

## Fase 6. Segurança (0,5 dia)

1. `dependency-check-maven` com relatório em `docs/security/`.
2. JWT: validar `issuer`, segredo com no mínimo 32 bytes, Swagger fora do profile `prod`.
3. Registrar achados e decisões. Nenhum relatório foi gerado até agora.

## Fase 7. Documentação e entregáveis (2 dias)

### 7.1 Ajustes na documentação DDD (`docs/Documentacao_DDD_Fase1.md`, `docs/ddd/linguagem-ubiqua.md`)

Cada ajuste vem de uma divergência da análise do Miro. Só alterar após as decisões da seção 9.

| Seção | Ajuste |
|---|---|
| §3 e `linguagem-ubiqua.md` | Incluir **Cancelamento** na lista de status (CANCELADA) e alinhar a definição de Funcionário ("responsável pela execução do serviço", ator autenticado) |
| §4 Event Storming | Trocar "Peça Reservada → Peça Consumida" por baixa na aprovação, sem reserva (D07). Incluir "Solicitar atendimento" como passo externo |
| §5 Bounded Contexts | Substituir "Orçamento" e "Execução da OS" pelos 5 BCs do Miro: Identidade e Acesso, Atendimento, Catálogo, Ordem de Serviço (com Diagnóstico e Orçamento), Estoque. Incluir o context map |
| §6 Entidades | Citar `Material` como equivalente a PecaInsumo `[D-E]`. Incluir Diagnóstico, ItemServico, ItemMaterial, HistoricoStatus |
| §7 Value Objects | Usar a lista do Miro: Documento, Placa, Dinheiro, Quantidade, StatusOrdemServico. Dizer quais estão implementados (hoje nenhum como classe) |
| §8 Aggregate Root | OrdemServico como raiz, com Orçamento e itens. Cliente, Veiculo, Servico, Material como agregados separados |
| §9 Eventos | Reescrever com os eventos do Miro (págs. 9 e 15). Declarar que são conceituais e não existem como classes `[D-I]` |
| §10 Regras | Incluir RN01 a RN14 da análise. Marcar como "decisão do time" as regras vindas de D-B, D-G, D-H |
| §11 Diagrama de estados | Incluir CANCELADA e a transição AGUARDANDO_APROVACAO → CANCELADA. Documentar Gerado = PENDENTE no orçamento |
| §13 DDD × código | Tabela conceito → classe real (`domain/<ctx>/model`, não `Domain/Entities`) |

### 7.2 Demais entregáveis

1. **README:** remover `dotnet`, `:5000/:5001`, "Java 17"; informar Java 21 obrigatório; Swagger em `:8080/swagger-ui.html`; justificativa do PostgreSQL e do H2 como conveniência de dev; credenciais de demo via variável de ambiente; estrutura real de diretórios; comandos de teste e cobertura; links do DDD e do vídeo; corrigir RMs duplicados.
2. **OpenAPI:** `@Tag` e `@Operation` nos controllers novos e exportação de `docs/api-docs`.
3. **Miro:** exportar em resolução maior (resolve a lacuna das págs. 5, 6, 11, 12, 14) e salvar em `docs/ddd/event-storming.pdf`, além de `diagramas.pdf`.
4. **Evidências:** captura do JaCoCo, relatório de vulnerabilidades, Swagger e fluxo JWT.
5. **Vídeo** (até 15 min, YouTube público) e **PDF final** (identificação, repositório, branch, hash do commit, vídeo, link do Miro).

## 8. Ordem, dependências e trilhas

```
0 ──► 1 ──► 2.2 (mover Orcamento) ──► 2.1 ──► 2.3 ──► 2.4 ──► 3 ──► 5 (integração)
0 ──► 4 ──► 6
2.1 ──► 5 (unitários de domínio, em paralelo à 2.3)
decisões (seção 9) ──► 7.1
```

| Trilha | Itens |
|---|---|
| A (núcleo) | 1 → 2 → 3 |
| B (infra e segurança) | 0 → 4 → 6 |
| C (qualidade) | 5, a partir da estabilização da 2.1 |
| D (docs e entrega) | 7 |

Estimativa: 10 a 12 dias-pessoa (o original tinha 9 a 10, o acréscimo vem da CANCELADA, do estoque na aprovação, do lock otimista e da refatoração 2.2).

## 9. Decisões que precisam do grupo

| Cód. | Decisão | Padrão assumido |
|---|---|---|
| D-A | Diagnóstico: atributo + `concluirDiagnostico()` ou entidade | Atributo |
| D-B | Estoque insuficiente na aprovação | Bloquear, OS segue aguardando |
| D-C | CANCELADA como 7º status | Sim |
| D-D | Início da execução | Automático |
| D-E | Material × PecaInsumo | Manter Material, documentar |
| D-F | Mover Orcamento para `ordemservico` | Sim (aprovação necessária) |
| D-G | Itens só em EM_DIAGNOSTICO | Sim |
| D-H | Finalizar exige ItemServico | Sim |
| D-I | Eventos de domínio | Só documentar |
| D-J | Notificação ao cliente | Fora do MVP |
| D-K | Re-exportar o Miro (págs. 5, 6, D06) | Sim |
| D-L | Quem registra a aprovação | Funcionário autenticado |
| D-M | Reescrever o histórico git | Decidir |

## 10. Riscos

1. **Fase 1 é o caminho crítico.** Sem persistir o agregado, nada do fluxo funciona.
2. **Cobertura 80%:** hoje 39,8%. Principal ameaça à nota.
3. **Segredos no histórico:** o `application.yml` atual traz segredo JWT e `admin123`.
4. **Doc divergente do código:** o enunciado diz que documentação que não corresponde ao código não conta como evidência. A Fase 7.1 só deve rodar depois do código, para refletir o que existe.
5. **Leitura incompleta do Miro:** hotspots das págs. 5 e 6 não lidos. Podem esconder regras ainda não cobertas.
6. **Refatoração 2.2** altera muitos imports. Fazer isolada e com build verde.

## 11. O que este plano não comprova

- Não executei a aplicação, o Docker nem os endpoints. As lacunas de endpoint e persistência vêm da leitura de código.
- Os números de cobertura e testes vêm da sessão anterior (JDK 21, `mvnw clean verify`) e não foram reexecutados agora.
- Itens com "conferir antes" (prefixo dos endpoints, assinatura de `ClienteRepository`, `@Version` em outras entities) dependem de leitura que ainda deve ser feita na implementação.
