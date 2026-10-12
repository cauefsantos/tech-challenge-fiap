# Entidades do Domínio - Fase 1

## Sistema de Gestão de Oficina Mecânica

## 1. Objetivo

Este documento apresenta as entidades priorizadas para a implementação da Fase 1 do sistema de gestão de oficina mecânica.

O modelo foi mantido propositalmente enxuto, concentrando-se nos requisitos necessários para:

- cadastrar clientes e veículos;
- criar e acompanhar Ordens de Serviço;
- registrar serviços, peças e insumos;
- gerar e aprovar orçamentos;
- controlar as alterações de status;
- manter o histórico financeiro e operacional da Ordem de Serviço.

As funcionalidades de maior complexidade, que não são essenciais para o primeiro recorte da solução, foram registradas como melhorias futuras.

---

## 2. Entidades priorizadas

As seguintes entidades devem fazer parte do código da Fase 1:

1. Cliente
2. Veículo
3. Ordem de Serviço
4. Serviço
5. Item de Serviço
6. Material
7. Item de Material
8. Orçamento
9. Histórico de Status

> Neste documento, **Material** representa tanto uma peça quanto um insumo. A distinção é realizada pelo atributo `tipo`. Essa abordagem reduz duplicações no primeiro recorte da aplicação sem eliminar a diferença dos conceitos na linguagem do domínio.

---

## 3. Cliente

Representa a pessoa física ou jurídica responsável pelo veículo e pela aprovação do orçamento.

### Atributos

- `id`: identificador único do cliente.
- `tipoPessoa`: indica se o cliente é pessoa física ou jurídica.
- `documento`: CPF ou CNPJ do cliente.
- `nome`: nome completo ou razão social.
- `email`: endereço eletrônico para contato.
- `telefone`: telefone para contato.
- `dataCadastro`: data e hora de criação do cadastro.
- `ativo`: indica se o cliente está ativo no sistema.

### Justificativa

A entidade é necessária para identificar o responsável pelo veículo, associar o cliente às Ordens de Serviço e registrar quem aprova ou rejeita o orçamento.

### Regras principais

- O documento deve ser válido de acordo com o tipo de pessoa.
- Não devem existir dois clientes ativos com o mesmo CPF ou CNPJ.
- Um cliente inativo deve permanecer disponível para consulta histórica.

---

## 4. Veículo

Representa o veículo atendido pela oficina e associado a um cliente.

### Atributos

- `id`: identificador único do veículo.
- `clienteId`: identificador do cliente responsável.
- `placa`: placa do veículo.
- `marca`: fabricante ou marca do veículo.
- `modelo`: modelo do veículo.
- `ano`: ano de fabricação ou ano-modelo adotado pelo projeto.
- `dataCadastro`: data e hora de criação do cadastro.
- `ativo`: indica se o veículo está ativo no sistema.

### Justificativa

A Ordem de Serviço deve estar relacionada a um veículo específico. A demanda também exige o cadastro do veículo com placa, marca, modelo e ano.

### Regras principais

- A placa deve possuir formato válido.
- Não devem existir dois veículos ativos com a mesma placa.
- O veículo deve estar vinculado a um cliente existente.

---

## 5. Ordem de Serviço

Representa o atendimento realizado pela oficina. É a entidade central do domínio e a raiz do agregado principal.

### Atributos

- `id`: identificador único da Ordem de Serviço.
- `numero`: código amigável utilizado para consulta e apresentação.
- `clienteId`: identificador do cliente responsável.
- `veiculoId`: identificador do veículo atendido.
- `status`: estado atual da Ordem de Serviço.
- `descricaoProblema`: descrição inicial do problema informado.
- `diagnostico`: resultado da avaliação técnica do veículo.
- `dataAbertura`: data e hora de abertura.
- `dataInicioExecucao`: data e hora de início da execução, quando aplicável.
- `dataFinalizacao`: data e hora de conclusão dos serviços, quando aplicável.
- `dataEntrega`: data e hora da entrega do veículo, quando aplicável.
- `valorTotal`: valor consolidado da Ordem de Serviço.
- `observacoes`: informações complementares do atendimento.
- `itensServico`: serviços incluídos na Ordem de Serviço.
- `itensMaterial`: peças e insumos incluídos na Ordem de Serviço.
- `orcamentos`: versões de orçamento associadas à Ordem de Serviço.
- `historicoStatus`: alterações de status registradas durante o atendimento.

### Status previstos

- `RECEBIDA`
- `EM_DIAGNOSTICO`
- `AGUARDANDO_APROVACAO`
- `EM_EXECUCAO`
- `FINALIZADA`
- `ENTREGUE`

### Justificativa

A Ordem de Serviço concentra o fluxo principal da demanda: recebimento, diagnóstico, orçamento, aprovação, execução, finalização e entrega.

### Regras principais

- Deve possuir cliente e veículo válidos.
- Deve respeitar as transições permitidas de status.
- Não pode entrar em execução sem orçamento aprovado.
- Não pode ser finalizada sem estar em execução.
- Não pode ser alterada depois de entregue.
- A inclusão ou remoção de itens deve atualizar o valor do orçamento ainda não aprovado.

---

## 6. Serviço

Representa uma atividade oferecida pela oficina, como troca de óleo, alinhamento ou revisão.

### Atributos

- `id`: identificador único do serviço.
- `codigo`: código interno utilizado pela oficina.
- `nome`: nome do serviço.
- `descricao`: descrição do trabalho realizado.
- `valorBase`: valor padrão do serviço.
- `tempoEstimado`: duração estimada para execução.
- `dataCadastro`: data e hora de criação do cadastro.
- `ativo`: indica se o serviço pode ser utilizado em novas Ordens de Serviço.

### Justificativa

A demanda exige o cadastro administrativo de serviços e a inclusão desses serviços na Ordem de Serviço e no orçamento.

### Regras principais

- O código deve ser único.
- O valor base não pode ser negativo.
- Serviços inativos não podem ser adicionados a novas Ordens de Serviço.

---

## 7. Item de Serviço

Representa um serviço incluído em uma Ordem de Serviço específica.

### Atributos

- `id`: identificador único do item.
- `ordemServicoId`: identificador da Ordem de Serviço.
- `servicoId`: identificador do serviço de catálogo.
- `descricao`: descrição preservada no momento da inclusão.
- `quantidade`: quantidade contratada.
- `valorUnitario`: valor aplicado à Ordem de Serviço.
- `subtotal`: resultado da quantidade multiplicada pelo valor unitário.
- `tempoEstimado`: tempo previsto no momento da inclusão.

### Justificativa

O serviço de catálogo pode mudar de nome, preço ou tempo estimado. O Item de Serviço preserva os dados efetivamente utilizados no orçamento e no atendimento.

### Regras principais

- A quantidade deve ser maior que zero.
- O valor unitário não pode ser negativo.
- O subtotal deve ser calculado a partir da quantidade e do valor unitário.
- Itens não devem ser alterados depois da aprovação do orçamento, salvo por um fluxo explícito de revisão.

---

## 8. Material

Representa uma peça ou um insumo disponível para utilização nos serviços da oficina.

### Atributos

- `id`: identificador único do material.
- `codigo`: código interno do material.
- `tipo`: indica se o material é uma peça ou um insumo.
- `nome`: nome do material.
- `descricao`: descrição complementar.
- `valorUnitario`: valor padrão por unidade.
- `unidadeMedida`: unidade utilizada, como unidade, litro ou quilograma.
- `quantidadeEstoque`: quantidade disponível no estoque.
- `estoqueMinimo`: quantidade mínima recomendada.
- `dataCadastro`: data e hora de criação do cadastro.
- `ativo`: indica se o material pode ser usado em novas Ordens de Serviço.

### Tipos previstos

- `PECA`
- `INSUMO`

### Justificativa

Peças e insumos participam do orçamento e precisam de controle mínimo de disponibilidade. Uma entidade comum reduz a duplicação entre conceitos que, neste primeiro recorte, compartilham os mesmos atributos e operações.

### Regras principais

- O código deve ser único.
- O tipo deve ser informado.
- O valor unitário não pode ser negativo.
- A quantidade em estoque não pode ser negativa.
- Materiais inativos não podem ser adicionados a novas Ordens de Serviço.

---

## 9. Item de Material

Representa uma peça ou um insumo incluído em uma Ordem de Serviço específica.

### Atributos

- `id`: identificador único do item.
- `ordemServicoId`: identificador da Ordem de Serviço.
- `materialId`: identificador do material de catálogo.
- `tipo`: tipo preservado no momento da inclusão.
- `descricao`: descrição preservada no momento da inclusão.
- `quantidade`: quantidade utilizada ou prevista.
- `valorUnitario`: valor aplicado à Ordem de Serviço.
- `subtotal`: resultado da quantidade multiplicada pelo valor unitário.

### Justificativa

A associação entre a Ordem de Serviço e o material possui quantidade, preço e subtotal próprios. Esses valores também precisam ser preservados caso o cadastro do material seja alterado posteriormente.

### Regras principais

- A quantidade deve ser maior que zero.
- O valor unitário não pode ser negativo.
- O subtotal deve ser calculado a partir da quantidade e do valor unitário.
- A inclusão deve verificar a disponibilidade do material.
- O consumo deve atualizar a quantidade disponível em estoque.

---

## 10. Orçamento

Representa a composição financeira de uma Ordem de Serviço e a decisão do cliente.

### Atributos

- `id`: identificador único do orçamento.
- `ordemServicoId`: identificador da Ordem de Serviço.
- `versao`: número sequencial da versão do orçamento.
- `valorServicos`: soma dos Itens de Serviço.
- `valorPecas`: soma dos Itens de Material do tipo peça.
- `valorInsumos`: soma dos Itens de Material do tipo insumo.
- `valorTotal`: soma final do orçamento.
- `status`: situação atual do orçamento.
- `dataGeracao`: data e hora da geração.
- `dataResposta`: data e hora da aprovação ou rejeição, quando aplicável.
- `observacaoResposta`: justificativa ou observação da decisão do cliente.

### Status previstos

- `PENDENTE`
- `DISPONIBILIZADO`
- `APROVADO`
- `REJEITADO`
- `CANCELADO`

### Justificativa

O orçamento é uma etapa explícita do fluxo. Ele consolida os valores, é disponibilizado ao cliente e determina se a execução da Ordem de Serviço pode começar.

### Regras principais

- O orçamento deve ser calculado a partir dos itens da Ordem de Serviço.
- Apenas um orçamento pode estar aprovado para a mesma Ordem de Serviço.
- Somente um orçamento disponibilizado pode ser aprovado ou rejeitado.
- Um orçamento aprovado não deve ter seus valores alterados.
- A execução da Ordem de Serviço exige um orçamento aprovado.

---

## 11. Histórico de Status

Representa o registro de cada alteração de status da Ordem de Serviço.

### Atributos

- `id`: identificador único do registro.
- `ordemServicoId`: identificador da Ordem de Serviço.
- `statusAnterior`: estado anterior, quando existente.
- `statusNovo`: estado assumido pela Ordem de Serviço.
- `dataAlteracao`: data e hora da transição.
- `observacao`: justificativa ou informação complementar da alteração.

### Justificativa

A Ordem de Serviço precisa ser acompanhada durante todo o ciclo de vida. O histórico permite saber quando cada mudança ocorreu e oferece os dados básicos para calcular o tempo de execução.

### Regras principais

- Cada transição válida deve produzir um registro.
- O histórico não deve ser alterado depois de criado.
- O status novo do último registro deve corresponder ao status atual da Ordem de Serviço.

---

## 12. Relacionamentos principais

    Cliente 1 ─── N Veículo

    Cliente 1 ─── N Ordem de Serviço

    Veículo 1 ─── N Ordem de Serviço

    Ordem de Serviço 1 ─── N Item de Serviço

    Serviço 1 ─── N Item de Serviço

    Ordem de Serviço 1 ─── N Item de Material

    Material 1 ─── N Item de Material

    Ordem de Serviço 1 ─── N Orçamento

    Ordem de Serviço 1 ─── N Histórico de Status

---

## 13. Organização sugerida no código

    domain/
    ├── cliente/
    │   ├── Cliente.java
    │   └── ClienteRepository.java
    ├── veiculo/
    │   ├── Veiculo.java
    │   └── VeiculoRepository.java
    ├── ordemservico/
    │   ├── OrdemServico.java
    │   ├── StatusOrdemServico.java
    │   ├── ItemServico.java
    │   ├── ItemMaterial.java
    │   ├── HistoricoStatus.java
    │   └── OrdemServicoRepository.java
    ├── servico/
    │   ├── Servico.java
    │   └── ServicoRepository.java
    ├── material/
    │   ├── Material.java
    │   ├── TipoMaterial.java
    │   └── MaterialRepository.java
    └── orcamento/
        ├── Orcamento.java
        └── StatusOrcamento.java

### Limite do agregado principal

A `OrdemServico` deve controlar:

- itens de serviço;
- itens de material;
- orçamentos;
- status atual;
- histórico de status;
- regras de transição;
- autorização para execução, finalização e entrega.

`Cliente`, `Veiculo`, `Servico` e `Material` possuem ciclos de vida próprios e devem ser referenciados por identificador dentro da Ordem de Serviço.

---

## 14. Objetos de valor recomendados

Mesmo com o escopo reduzido, alguns atributos podem ser modelados como objetos de valor para centralizar validações:

- `Documento`: representa e valida CPF ou CNPJ.
- `Placa`: representa e valida a placa do veículo.
- `Email`: representa e valida o endereço eletrônico.
- `Dinheiro`: representa valores monetários e evita operações inconsistentes.

Esses objetos não são novas entidades, pois não possuem identidade ou ciclo de vida independente.

Caso o prazo seja limitado, eles podem ser introduzidos gradativamente, começando por `Documento` e `Placa`, que possuem validações explicitamente relacionadas à demanda.

---

## 15. Melhorias futuras ou despriorizadas

Os elementos abaixo não fazem parte do primeiro recorte do código. Eles poderão ser adicionados quando os requisitos básicos estiverem implementados, testados e documentados.

### 15.1 Diagnóstico como entidade independente

No primeiro recorte, o diagnóstico pode ser armazenado diretamente na Ordem de Serviço.

Transformá-lo em entidade será útil se o sistema precisar de:

- vários diagnósticos para a mesma Ordem de Serviço;
- revisão ou versionamento;
- responsáveis diferentes;
- datas próprias de início e conclusão;
- anexos ou evidências técnicas.

### 15.2 Funcionário

Foi despriorizado enquanto não houver necessidade explícita de atribuir e auditar cada operação por funcionário.

No futuro poderá registrar:

- responsável pelo diagnóstico;
- responsável pela execução;
- responsável pela alteração de status;
- cargo e especialidade.

### 15.3 Usuário, perfil e permissões persistidos

A autenticação JWT pode inicialmente utilizar uma configuração simples. A persistência de usuários e perfis será necessária quando houver gestão real de acessos e autorização por função.

Possíveis perfis futuros:

- administrador;
- atendente;
- mecânico;
- estoquista.

### 15.4 Saldo e movimentação de estoque

Na Fase 1, o saldo pode ser mantido no próprio `Material`.

Entidades específicas de estoque serão úteis para:

- reserva de materiais;
- entrada e devolução;
- auditoria de movimentações;
- controle de concorrência;
- múltiplos depósitos;
- rastreamento de quem alterou o estoque.

### 15.5 Endereço

O endereço do cliente pode ser adicionado se houver requisito de cobrança, entrega, emissão de documento ou localização. Não é necessário para o fluxo principal da Ordem de Serviço neste primeiro recorte.

### 15.6 Responsável pela alteração de status

O histórico inicial registra status, data e observação. A referência ao funcionário ou usuário que realizou a mudança poderá ser adicionada junto com a gestão persistida de funcionários e acessos.

### 15.7 Recursos adicionais de orçamento

Podem ser adicionados posteriormente:

- data de validade;
- descontos;
- impostos;
- responsável pela aprovação;
- comprovante da aprovação;
- motivo estruturado de rejeição;
- expiração automática.

---

## 16. Resumo da priorização

### Implementar na Fase 1

- Cliente
- Veículo
- Ordem de Serviço
- Serviço
- Item de Serviço
- Material
- Item de Material
- Orçamento
- Histórico de Status

### Despriorizar neste primeiro momento

- Diagnóstico como entidade independente
- Funcionário
- Usuário e perfis persistidos
- Saldo de Estoque separado
- Movimentação de Estoque
- Endereço
- Auditoria completa por usuário
- Recursos avançados de orçamento

---

## 17. Conclusão

O modelo priorizado cobre o núcleo da demanda sem introduzir complexidade desnecessária. A Ordem de Serviço permanece como o centro do domínio, acompanhada das entidades necessárias para representar cliente, veículo, serviços, materiais, orçamento e evolução de status.

As melhorias futuras foram registradas para evitar que decisões temporárias sejam confundidas com limitações definitivas. A equipe poderá evoluir o modelo conforme surgirem requisitos de auditoria, gestão de funcionários, segurança persistida e controle detalhado de estoque.
