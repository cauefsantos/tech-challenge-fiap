# Checklist de Entregáveis - Fase 1 Tech Challenge

## 1. PDF Final de Entrega
- [ ] Entregar **um único arquivo PDF**.
- [ ] Incluir todos os links necessários para avaliação.
- [ ] Validar que todos os links estão acessíveis.

## 2. Identificação da Entrega (Primeira Página do PDF)
- [ ] Curso: SOAT — Software Architecture.
- [ ] Fase 1.
- [ ] Número do grupo.
- [ ] Nome completo de todos os integrantes.
- [ ] RM de todos os integrantes.
- [ ] Username do Discord de cada integrante.
- [ ] Linguagem utilizada.
- [ ] Principais tecnologias utilizadas.

## 3. Repositório do Projeto
- [ ] Link completo e clicável do repositório.
- [ ] Repositório público no GitHub.
- [ ] Branch, tag ou commit identificando a versão entregue.
- [ ] Branch e hash do commit informados no PDF.
- [ ] Código compatível com vídeo e documentação.
- [ ] Sem credenciais, senhas ou informações sensíveis.
- [ ] README.md completo.
- [ ] Instruções para execução local.
- [ ] Testes automatizados com cobertura mínima de 80%.
- [ ] Swagger/OpenAPI disponível.
- [ ] Relatório ou resultado da análise de vulnerabilidades.
- [ ] Main protegida e fluxo via Pull Request.

## 4. README.md
- [ ] Objetivo do projeto.
- [ ] Descrição resumida da solução.
- [ ] Arquitetura utilizada.
- [ ] Tecnologias utilizadas.
- [ ] Pré-requisitos.
- [ ] Instruções de execução.
- [ ] Instruções de execução dos testes.
- [ ] Instruções para acesso ao Swagger.
- [ ] Justificativa da escolha do banco de dados.
- [ ] Usuários/credenciais de demonstração ou autenticação.
- [ ] Estrutura dos diretórios principais.
- [ ] Link da documentação DDD.
- [ ] Link do vídeo.

## 5. Vídeo da Entrega
- [ ] Link completo e clicável no PDF.
- [ ] Publicado como Público no YouTube.
- [ ] Máximo de 15 minutos.
- [ ] Áudio compreensível.
- [ ] Demonstração real da aplicação.
- [ ] Projeto correspondente ao commit informado.
- [ ] Identificação dos integrantes.

### Conteúdo mínimo do vídeo
- [ ] Proposta da solução.
- [ ] Arquitetura do projeto.
- [ ] Organização do monólito em camadas.
- [ ] Escolha e justificativa do banco de dados.
- [ ] Event Storming.
- [ ] Elementos de DDD.
- [ ] Fluxo de criação da OS.
- [ ] Fluxo de acompanhamento da OS.
- [ ] Inclusão de serviços.
- [ ] Inclusão de peças e insumos.
- [ ] Geração do orçamento.
- [ ] Aprovação do orçamento.
- [ ] Alteração de status da OS.
- [ ] Autenticação JWT.
- [ ] Execução da aplicação.
- [ ] Swagger/OpenAPI.
- [ ] Testes automatizados.
- [ ] Resultado da análise de vulnerabilidades.

## 6. Documentação DDD
- [ ] Link direto no PDF.
- [ ] Documento acessível sem solicitar permissão.
- [ ] Exportação armazenada no repositório.
- [ ] Event Storming da criação da OS.
- [ ] Event Storming do acompanhamento da OS.
- [ ] Event Storming da gestão de peças e insumos.
- [ ] Eventos de domínio.
- [ ] Comandos.
- [ ] Políticas/Regras de negócio.
- [ ] Atores.
- [ ] Agregados.
- [ ] Entidades.
- [ ] Objetos de Valor.
- [ ] Bounded Contexts (quando aplicável).
- [ ] Diagramas da disciplina.
- [ ] Linguagem Ubíqua.
- [ ] Glossário do domínio.
- [ ] Relação entre documentação e implementação.

## 7. Funcionalidades Obrigatórias

### Criação da Ordem de Serviço
- [ ] Identificação do cliente por CPF/CNPJ.
- [ ] Cadastro de veículo (placa, marca, modelo e ano).
- [ ] Inclusão de serviços.
- [ ] Inclusão de peças e insumos.
- [ ] Geração automática do orçamento.
- [ ] Disponibilização/envio do orçamento.
- [ ] Aprovação ou rejeição do orçamento.

### Acompanhamento da Ordem de Serviço
- [ ] Status Recebida.
- [ ] Status Em diagnóstico.
- [ ] Status Aguardando aprovação.
- [ ] Status Em execução.
- [ ] Status Finalizada.
- [ ] Status Entregue.
- [ ] Controle de transições de status.
- [ ] Regras de transição implementadas.
- [ ] Consulta do andamento via API.

### Gestão Administrativa
- [ ] CRUD de clientes.
- [ ] CRUD de veículos.
- [ ] CRUD de serviços.
- [ ] CRUD de peças e insumos.
- [ ] Controle de estoque.
- [ ] Listagem de Ordens de Serviço.
- [ ] Detalhamento de Ordens de Serviço.
- [ ] Tempo médio de execução dos serviços.

## 8. Requisitos Técnicos Obrigatórios
- [ ] Back-end monolítico.
- [ ] Arquitetura em camadas.
- [ ] Banco de dados com justificativa documentada.
- [ ] APIs RESTful.
- [ ] Swagger/OpenAPI.
- [ ] Autenticação JWT.
- [ ] Validação de CPF/CNPJ.
- [ ] Validação de placa de veículo.
- [ ] Testes unitários.
- [ ] Testes de integração.
- [ ] Cobertura mínima de 80% nos domínios críticos.
- [ ] Instruções para execução local.
- [ ] Tratamento adequado de transações.

## 9. Evidências Recomendadas
- [ ] Screenshot ou relatório de cobertura de testes.
- [ ] Relatório de análise de vulnerabilidades.
- [ ] Swagger funcionando.
- [ ] Dockerfile.
- [ ] docker-compose.yml.
- [ ] Evidência de autenticação JWT.
- [ ] Evidência de consulta de status da OS.
