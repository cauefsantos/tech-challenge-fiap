# Análise do `Miro - Fase 01.pdf` (Event Storming)

Fonte: `.dev/Miro - Fase 01.pdf` (16 páginas). Data da análise: 2026-10-19.

## Limites desta análise (ler antes de usar)

- A leitura foi feita sobre a renderização padrão do PDF. **Não houve renderização com zoom nem extração de imagens em resolução nativa**: o `bash` do agente leitor foi bloqueado por regra de permissão do ambiente.
- **Ilegíveis:** conteúdo dos cartões das págs. 5 e 6 (hotspots, post-its, dois modelos de leitura verdes, cartão rosa "?" e cerca de 5 PAs vermelhos).
- **Baixa confiança:** texto exato de D06 (pág. 3), págs. 11 (agregados), 12 (context map) e 14 (máquina de estados). Os rótulos dessas páginas foram inferidos.
- **Confiança boa:** págs. 1, 2, 4, 7, 8, 9, 10, 13 e 15 (a 7 e a 15 com algum esforço), 16 (parcial, mas coerente com a pág. 1).
- Para fechar as lacunas: exportar o board do Miro em resolução maior, ou liberar o `bash` e renderizar as págs. 3, 5, 6, 11, 12 e 14 com PyMuPDF.
- Duas afirmações da análise foram conferidas no código nesta sessão: `StatusOrdemServico` não tem `CANCELADA`, e há dependência circular entre `domain.orcamento` e `domain.ordemservico`.

## 1. Conteúdo por página

| Pág. | Conteúdo | Legibilidade |
|---|---|---|
| 1 | Glossário: Cliente, Veículo, OS, Diagnóstico, Serviço, Peça, Insumo, Orçamento, Aprovação, Execução, Finalização, Entrega, Estoque, Funcionário ("responsável pela execução do serviço"), Status (6 valores) e Cancelamento ("encerramento da OS sem execução dos serviços, decorrente da rejeição do orçamento pelo cliente") | Boa |
| 2 | Objetivo: proposta inicial que valida o fluxo principal, termos, primeiros eventos e dúvidas pendentes. Elementos preliminares | Boa |
| 3 | Decisões D01 a D07 | D06 parcial |
| 4 | Legenda: EV, CMD, POL, ML, AT, PA, SE, AG e "Eventos Pivotais" | Boa |
| 5 | Visão geral: faixa do Happy Path (~15 cartões) e faixa de Hotspots (~13 post-its) | Títulos legíveis, cartões ilegíveis |
| 6 | Linha do tempo colorida com ramificação aprovar/rejeitar | Majoritariamente ilegível |
| 7 | Eventos → Políticas | Legível com esforço |
| 8 | Fluxo de comandos | Boa |
| 9 | Fluxo de eventos | Boa |
| 10 | Entidades → VOs | Boa |
| 11 | Agregados → raiz → entidades/VOs | Baixa confiança |
| 12 | Context map | Baixa confiança |
| 13 | Bounded contexts | Boa |
| 14 | Máquina de estados da OS | Parcial |
| 15 | Event Storming de estoque | Legível com esforço |
| 16 | Fluxo macro em 10 etapas | Parcial |

## 2. Decisões (pág. 3)

| Cód. | Decisão |
|---|---|
| D01 | A OS pode ser criada antes do diagnóstico, com cliente, veículo e informações do atendimento |
| D02 | Serviços identificados no diagnóstico podem ser adicionados à OS |
| D03 | Peças e insumos são definidos após o diagnóstico |
| D04 | Orçamento aprovado ou rejeitado **integralmente**. Sem aprovação parcial |
| D05 | Rejeição do orçamento **cancela a OS** |
| D06 | Sem diferenciação de papéis operacionais no MVP: usuários internos são "funcionário" (baixa confiança, redação exata ilegível) |
| D07 | Estoque baixado **após a aprovação** do orçamento. Antes disso peças e insumos não alteram a quantidade disponível. **A aprovação depende de estoque suficiente** |

Não há códigos "R-xx" no PDF.

## 3. Elementos do Event Storming

**Atores:** Funcionário; Cliente (aprova ou rejeita o orçamento).

**Comandos (pág. 8):** Solicitar atendimento → Cadastrar cliente → Cadastrar veículo → Criar OS → Iniciar diagnóstico → Concluir diagnóstico → Adicionar serviço → Adicionar peça/insumo → Gerar orçamento → Disponibilizar orçamento → [Aprovar orçamento → Iniciar execução + Baixar estoque → Finalizar OS → Registrar entrega] ou [Rejeitar orçamento → Cancelar OS]. Paralelo: Cadastrar peça/insumo → Atualizar estoque. Pág. 15 acrescenta Atualizar quantidade em estoque, Verificar estoque, Baixar peças e insumos.

**Eventos (pág. 9):** Atendimento solicitado → Cliente cadastrado → Veículo associado → OS criada → Diagnóstico iniciado → Diagnóstico concluído → Serviços adicionados → Peças e insumos adicionados → Orçamento gerado → Orçamento disponibilizado → [Orçamento aprovado → Execução iniciada → OS finalizada → Veículo entregue; também → Estoque atualizado] ou [Orçamento rejeitado → OS cancelada]. Ramo de estoque: Peça/insumo cadastrado → Estoque atualizado → Estoque insuficiente identificado. Pág. 15 acrescenta Estoque disponível. Pág. 7 menciona Serviços concluídos.

**Políticas (pág. 7):**

| Evento | Política |
|---|---|
| OS criada | Definir status Recebida |
| Diagnóstico iniciado | Status Em diagnóstico |
| Orçamento disponibilizado | Status Aguardando aprovação |
| Orçamento aprovado | Validar disponibilidade → Baixar estoque → Status Em execução |
| Orçamento rejeitado | Cancelar OS |
| Serviços concluídos | Status Finalizada |
| Veículo entregue | Status Entregue |

**Modelos de leitura e hotspots:** ML "Lista de peças e insumos" (pág. 15). PA "Definir tratamento para estoque insuficiente" (pág. 15). Os demais ML e PAs da pág. 6 estão ilegíveis. Nenhum sistema externo (SE) identificado.

**Entidades → VOs (pág. 10):** Veiculo→Placa; PecaInsumo→Quantidade e Dinheiro; Servico→Dinheiro; Orcamento→Dinheiro; Cliente→Documento; OrdemServico→StatusOrdemServico.

**Agregados (pág. 11, baixa confiança):** OrdemServico (raiz) contendo Diagnóstico, Orçamento, ItemServico, ItemPecaInsumo, StatusOrdemServico e Dinheiro. Agregados separados: Cliente, Veiculo, Servico, PecaInsumo.

**Bounded contexts (pág. 13):** Identidade e Acesso (Funcionário, Autenticação); Atendimento (Cliente, Veículo); Catálogo (Serviços); Ordem de Serviço (OS, Diagnóstico, Orçamento); Estoque (Peças, Insumos, Quantidade). Relações (rótulos de baixa confiança): Identidade autentica as operações dos demais; Atendimento fornece Cliente e Veículo à OS; Catálogo fornece Serviços e preços à OS; OS consulta e baixa itens do Estoque.

**Máquina de estados (pág. 14, baixa confiança):** Recebida → Em diagnóstico → Aguardando aprovação → Em execução → Finalizada → Entregue; Aguardando aprovação → **Cancelada** (rejeição).

## 4. Regras de negócio extraídas

| Cód. | Regra | Origem |
|---|---|---|
| RN01 | A OS nasce Recebida e pode ser criada antes do diagnóstico | D01, pág. 7 |
| RN02 | Iniciar diagnóstico: Recebida → Em diagnóstico | págs. 7, 14 |
| RN03 | Serviços, peças e insumos entram após ou durante o diagnóstico, não em Recebida | D02, D03, págs. 8, 9 |
| RN04 | Existe o passo explícito "Concluir diagnóstico" | págs. 8, 9 |
| RN05 | Orçamento é gerado e depois disponibilizado. Disponibilizar leva a Aguardando aprovação | págs. 7-9 |
| RN06 | Aprovação ou rejeição integral | D04 |
| RN07 | Rejeição cancela a OS, sem execução | D05 |
| RN08 | Aprovação depende de estoque suficiente. Ordem: verificar, baixar, Em execução | D07, págs. 7, 15 |
| RN09 | Antes da aprovação o estoque não é alterado. Sem reserva | D07 |
| RN10 | Estoque insuficiente gera evento próprio. **Tratamento indefinido** | pág. 15 |
| RN11 | Serviços concluídos → Finalizada. Entrega → Entregue | págs. 7, 14 |
| RN12 | Funcionário cadastra peças/insumos e atualiza quantidade | pág. 15 |
| RN13 | Operações administrativas exigem autenticação | págs. 12, 13 |
| RN14 | Sem papéis distintos entre funcionários no MVP | D06 (baixa confiança) |

O board **não traz regra de notificação** nem consulta pública do cliente. Esses itens vêm do enunciado (`docs/Fase 1.md`).

## 5. Divergências entre Miro, documentação e código

| # | Miro | Documentação / código | Ajuste |
|---|---|---|---|
| 1 | Status Cancelada por rejeição | `StatusOrdemServico` tem 6 valores, sem CANCELADA. `OrdemServico` não tem método que reaja a `Orcamento.rejeitar`. `Documentacao_DDD_Fase1.md` §11 também omite | Incluir CANCELADA terminal e `rejeitarOrcamento` na OS. Atualizar §11 |
| 2 | Aprovação exige estoque e baixa | `Material.validarDisponibilidade/consumir` existem, mas nenhum use case os chama. `Documentacao_DDD_Fase1.md` §4 fala em "Peça Reservada" | Remover "reserva". Verificar e baixar na aprovação, mesma transação |
| 3 | Iniciar execução é política após a baixa | `aprovarOrcamento()` chama `iniciarExecucao()` direto | Compatível. Documentar como automático |
| 4 | Iniciar e Concluir diagnóstico como comandos e eventos | Só `enviarParaDiagnostico` e `registrarDiagnostico`. Diagnóstico é texto na OS | Decisão pendente (D-A abaixo) |
| 5 | Itens só após o diagnóstico | `adicionarItemServico/Material` só bloqueia após aprovação ou entrega | Restringir a EM_DIAGNOSTICO |
| 6 | Orçamento dentro do agregado OS e do BC Ordem de Serviço | `Documentacao_DDD` §5 separa "Orçamento" e "Execução". No código, `Orcamento` importa `ordemservico.model` e `OrdemServico` importa `orcamento.model`: **dependência circular entre pacotes** | Alinhar BCs ao Miro. Mover `Orcamento` para `domain.ordemservico.model` (refatoração, exige aprovação) |
| 7 | PecaInsumo / ItemPecaInsumo | Doc e código usam Material, ItemMaterial, TipoMaterial | Decisão pendente (D-E) |
| 8 | VOs Documento, Placa, Dinheiro, Quantidade, StatusOrdemServico | Doc §7 lista CPF e CNPJ separados, sem Quantidade. Código usa `BigDecimal` | Alinhar §7. Implementar VOs só após testes verdes |
| 9 | Funcionário é ator e BC Identidade e Acesso (D06) | `ENTIDADES_FASE1.md` §15.2 despriorizou Funcionário | Tratar como ator autenticado via JWT, sem entidade persistida |
| 10 | Lista de eventos do Miro | `Documentacao_DDD` §9 tem 7 eventos com nomes diferentes. O código não tem classes de evento | Reescrever §9 com os eventos do Miro |
| 11 | Orçamento gerado → disponibilizado → aprovado/rejeitado | Enum tem PENDENTE, DISPONIBILIZADO, APROVADO, REJEITADO, CANCELADO | Documentar Gerado = PENDENTE |
| 12 | Solicitar atendimento / Atendimento solicitado | Sem equivalente | Documentar como passo externo ao sistema |
| 13 | Serviços concluídos → Finalizada | Código não exige itens de serviço para finalizar | Decisão pendente (D-H) |
| 14 | Sem histórico de status nem tempo médio | Código tem `HistoricoStatus`, e o enunciado exige tempo médio | Acrescentar ML de acompanhamento e tempo médio na doc |
| 15 | ML da pág. 6 ilegíveis | Enunciado exige consulta do cliente | Documentar ML "Consulta de status da OS" |

Observação de banco: `ordem_servico.status` é `VARCHAR(40) NOT NULL` sem CHECK (`V1__create_ordem_servico.sql:57`). Incluir CANCELADA no enum **não exige migration de constraint**. O CHECK de `orcamento.status` (linha 101) já admite REJEITADO.

## 6. Decisões abertas

| Cód. | Pergunta | Opções |
|---|---|---|
| D-A | Diagnóstico: atributo da OS ou entidade? Precisa de comando "Concluir diagnóstico"? | Atributo + método `concluirDiagnostico()` (recomendado, menor impacto) / entidade nova |
| D-B | Estoque insuficiente na aprovação | Bloquear aprovação e manter Aguardando aprovação (recomendado) / voltar a Em diagnóstico / aprovar e aguardar peça |
| D-C | Cancelada entra como 7º status? | Sim (Miro e D05 exigem) |
| D-D | Início da execução | Automático na aprovação (como hoje) / comando separado |
| D-E | Nome: Material ou PecaInsumo | Manter Material e documentar equivalência (recomendado) / renomear tudo |
| D-F | Orçamento no pacote `ordemservico` | Mover (resolve ciclo) / manter e documentar |
| D-G | Itens em Recebida | Bloquear (recomendado, D02/D03) |
| D-H | Finalizar sem itens de serviço | Exigir pelo menos um ItemServico |
| D-I | Eventos de domínio | Só documentar (recomendado para o MVP) / implementar classes |
| D-J | Notificação ao cliente | Fora do escopo, basta consulta pública |
| D-K | Texto exato de D06 e págs. 5 e 6 | Depende de novo export do Miro |
