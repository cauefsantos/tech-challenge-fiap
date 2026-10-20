# Decisões de negócio e planejamento da Fase 1

Este documento consolida as decisões assumidas a partir dos planos em `.dev/plans` e do ajuste da implementação atual. O objetivo é alinhar documentação, regras de domínio e configuração executável do projeto.

## Decisões adotadas

- A aprovação do orçamento continua sendo realizada por usuário autenticado do sistema, não por endpoint público do cliente.
- O estoque é validado e consumido na aprovação do orçamento, conforme análise do Miro e do plano revisado.
- A OS entra em `CANCELADA` quando o orçamento é rejeitado.
- Itens de serviço e material são adicionados somente enquanto a ordem de serviço está em diagnóstico.
- O diagnóstico é tratado como atributo da ordem de serviço, com conclusão explícita por método do agregado.
- O orçamento permanece no agregado da ordem de serviço, conforme recomendação da análise do Miro para evitar dependências circulares.
- O material é mantido como nome de domínio; o equivalente a "Peça/Insumo" é documentado como `Material`.
- O início da execução é disparado automaticamente após a aprovação do orçamento.
- A finalização exige ao menos um item de serviço.

## Checklist de integração com os planos

- [x] Fase 0: variáveis de ambiente e `.env` foram implementadas.
- [x] Fase 1: persistência do agregado OS foi concluída no adapter e validada por teste de integração.
- [x] Fase 2: regras de domínio do fluxo da OS foram alinhadas ao plano revisado e os testes foram ajustados para refletir o estado atual.
- [x] Fase 2 (continuação): itens de serviço/material, listagem e endpoints do fluxo foram adicionados ao controller e ao use case.
- [x] Fase 2 (aprovação): validação de estoque e consumo na aprovação do orçamento foi implementada no caso de uso de fluxo da OS.
- [x] Fase 3: consulta pública por número e documento e métrica de tempo médio de execução da OS foram adicionadas na API.
- [x] Fase 4: Dockerfile e Compose foram adicionados como base para execução local/prod.
- [x] Documentação: este arquivo e o README foram atualizados para refletir o estado e as decisões aplicáveis.

## Status final do plano

O projeto está em estado funcional e coerente com a linha de trabalho definida em `.dev/plans`, com os itens prioritários do fluxo de Ordem de Serviço implementados e validados por testes.

## Observações

- A senha e o segredo JWT não devem ser versionados; use o arquivo `.env` local com valores reais.
- O plano revisado ainda aponta itens de refinamento e expansão para versões seguintes, mas os alvos principais da Fase 1 e da sequência de fluxo da OS foram concluídos no código e na documentação.
