# Documentação DDD - Fase 1 Tech Challenge
## Sistema de Gestão de Oficina Mecânica

## 1. Introdução

Este documento apresenta a modelagem de domínio da solução proposta para o gerenciamento de Ordens de Serviço (OS) de uma oficina mecânica.

O objetivo é aplicar os conceitos de Domain-Driven Design (DDD) para compreender o domínio do negócio, documentar os processos principais e orientar a implementação da aplicação monolítica baseada em APIs REST.

## 2. Contexto do Problema

A oficina necessita controlar todo o ciclo de vida de uma manutenção veicular:

- Cadastro de clientes
- Cadastro de veículos
- Abertura de Ordem de Serviço
- Inclusão de serviços
- Inclusão de peças e insumos
- Geração de orçamento
- Aprovação do orçamento
- Execução do serviço
- Controle de status
- Entrega do veículo

## 3. Linguagem Ubíqua

| Termo | Definição |
|---------|---------|
| Cliente | Pessoa física ou jurídica proprietária do veículo |
| Veículo | Automóvel associado a um cliente |
| Ordem de Serviço (OS) | Registro de manutenção executada pela oficina |
| Serviço | Atividade executada durante a manutenção |
| Peça | Componente utilizado no reparo |
| Insumo | Material consumido durante o serviço |
| Orçamento | Valor estimado da manutenção |
| Diagnóstico | Avaliação inicial do problema |
| Estoque | Controle de peças e insumos disponíveis |
| Status | Estado atual da Ordem de Serviço |

## 4. Event Storming

### Criação da Ordem de Serviço
Cliente Identificado → Veículo Cadastrado → Ordem de Serviço Criada → Serviço Adicionado → Peça Adicionada → Orçamento Calculado → Orçamento Disponibilizado → Orçamento Aprovado.

### Acompanhamento da Ordem de Serviço
Recebida → Em Diagnóstico → Aguardando Aprovação → Em Execução → Finalizada → Entregue.

### Gestão de Peças e Insumos
Peça Cadastrada → Estoque Atualizado → Peça Reservada → Peça Consumida.

## 5. Bounded Contexts

- Atendimento
- Orçamento
- Execução da Ordem de Serviço
- Estoque

## 6. Entidades

- Cliente
- Veículo
- OrdemServico
- Serviço
- Peça

## 7. Value Objects

- CPF
- CNPJ
- Placa
- Dinheiro

## 8. Aggregate Root

OrdemServico

## 9. Eventos de Domínio

- OrdemServicoCriada
- ServicoAdicionado
- PecaAdicionada
- OrcamentoCalculado
- OrcamentoAprovado
- OrdemServicoFinalizada
- OrdemServicoEntregue

## 10. Regras de Negócio

- Somente ordens aprovadas podem iniciar execução.
- Não finalizar OS sem serviços executados.
- Não utilizar peças sem estoque.
- Não alterar OS entregue.

## 11. Diagrama de Estados

Recebida → Em Diagnóstico → Aguardando Aprovação → Em Execução → Finalizada → Entregue

## 12. Arquitetura

API REST → Application → Domain → Infrastructure → Database

## 13. Relacionamento DDD x Código

Domain/Entities
Domain/ValueObjects
Domain/Events
Domain/Repositories

## 14. Conclusão

A Ordem de Serviço é o principal agregado do domínio e concentra as principais regras de negócio da solução.
