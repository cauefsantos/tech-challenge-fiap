# Checklist de Entregáveis - Fase 1 Tech Challenge

> Critério de marcação: `[x]` indica item comprovável no código, README ou documentação versionada deste projeto. Itens que dependem de entrega externa, identificação do grupo, configuração do GitHub ou evidência não versionada permanecem desmarcados.

## 1. PDF Final de Entrega
<!-- TODO: Montar o PDF final único, incluir os links de avaliação e testar cada link antes do envio. -->
- [ ] Entregar **um único arquivo PDF**.
- [ ] Incluir todos os links necessários para avaliação.
- [ ] Validar que todos os links estão acessíveis.

## 2. Identificação da Entrega (Primeira Página do PDF)
<!-- TODO: Substituir pelos dados confirmados do grupo e dos integrantes; eles não estão preenchidos no repositório. -->
- [ ] Curso: SOAT — Software Architecture.
- [ ] Fase 1.
- [ ] Número do grupo.
- [ ] Nome completo de todos os integrantes.
- [ ] RM de todos os integrantes.
- [ ] Username do Discord de cada integrante.
- [ ] Linguagem utilizada.
- [ ] Principais tecnologias utilizadas.

## 3. Repositório do Projeto
<!-- TODO: Confirmar URL clicável e visibilidade pública; escolher a versão de entrega, registrar branch/hash no PDF e conferir que código, documentação e vídeo correspondem a ela. -->
- [ ] Link completo e clicável do repositório.
- [ ] Repositório público no GitHub.
- [ ] Branch, tag ou commit identificando a versão entregue.
- [ ] Branch e hash do commit informados no PDF.
- [ ] Código compatível com vídeo e documentação.
- [x] Sem credenciais, senhas ou informações sensíveis versionadas (`.env` ignorado; `.env.example` contém placeholders).
<!-- TODO: Completar e revisar o README; ainda existem placeholders e links/documentos que precisam ser conferidos (incluindo links DDD e evidências de segurança). -->
- [ ] README.md completo.
- [x] Instruções para execução local.
- [x] Testes automatizados com cobertura mínima de 80% nos pacotes `domain.*` e `application.*` (resultado registrado no README).
- [x] Swagger/OpenAPI disponível e habilitado para avaliação.
- [x] Relatório ou resultado da análise de vulnerabilidades (`docs/dependency-check-report.html`).
<!-- TODO: Confirmar proteção da branch main e que as alterações são integradas via Pull Request nas configurações do GitHub. -->
- [ ] Main protegida e fluxo via Pull Request.

## 4. README.md
- [x] Objetivo do projeto.
- [x] Descrição resumida da solução.
- [x] Arquitetura utilizada.
- [x] Tecnologias utilizadas.
- [x] Pré-requisitos.
- [x] Instruções de execução.
- [x] Instruções de execução dos testes.
- [x] Instruções para acesso ao Swagger (URL documentada; habilitação depende da configuração do perfil).
- [x] Justificativa da escolha do banco de dados (PostgreSQL para execução próxima à produção e H2 para desenvolvimento/testes).
- [x] Usuários/credenciais de demonstração ou autenticação (documenta o fluxo JWT e configuração explícita de credenciais, sem credenciais padrão).
- [x] Estrutura dos diretórios principais.
<!-- TODO: Adicionar link válido para a documentação DDD e link público do vídeo no README. -->
- [ ] Link da documentação DDD.
- [ ] Link do vídeo.

## 5. Vídeo da Entrega
<!-- TODO: Produzir/publicar o vídeo como público, validar duração, áudio, acesso, commit demonstrado e identificação do grupo. -->
- [ ] Link completo e clicável no PDF.
- [ ] Publicado como Público no YouTube.
- [ ] Máximo de 15 minutos.
- [ ] Áudio compreensível.
- [ ] Demonstração real da aplicação.
- [ ] Projeto correspondente ao commit informado.
- [ ] Identificação dos integrantes.

### Conteúdo mínimo do vídeo
<!-- TODO: Gravar a demonstração cobrindo a proposta, arquitetura/DDD, fluxos funcionais, autenticação, execução, Swagger, testes e análise de vulnerabilidades. -->
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
<!-- TODO: Adicionar ao PDF o link direto da documentação e confirmar que o acesso externo não exige permissão. -->
- [ ] Link direto no PDF.
- [ ] Documento acessível sem solicitar permissão.
- [x] Exportação armazenada no repositório (`docs/ddd/event-storming.pdf` e documentação Markdown em `docs/`).
- [x] Event Storming da criação da OS (`docs/Documentacao_DDD_Fase1.md`).
- [x] Event Storming do acompanhamento da OS (`docs/Documentacao_DDD_Fase1.md`).
- [x] Event Storming da gestão de peças e insumos (`docs/Documentacao_DDD_Fase1.md`).
- [x] Eventos de domínio (`docs/Documentacao_DDD_Fase1.md`).
- [ ] Comandos.
- [x] Políticas/Regras de negócio (`docs/Documentacao_DDD_Fase1.md` e `docs/decisoes-fase1.md`).
- [ ] Atores.
- [x] Agregados (`OrdemServico` identificado como Aggregate Root).
- [x] Entidades (`docs/Documentacao_DDD_Fase1.md`).
- [x] Objetos de Valor (CPF, CNPJ, placa e dinheiro identificados na documentação DDD).
- [x] Bounded Contexts (documentados em `docs/Documentacao_DDD_Fase1.md`).
- [x] Diagramas da disciplina (`docs/ddd/event-storming.pdf` e diagrama de estados em `docs/Documentacao_DDD_Fase1.md`).
- [x] Linguagem Ubíqua (`docs/ddd/linguagem-ubiqua.md`).
- [x] Glossário do domínio (`docs/Documentacao_DDD_Fase1.md` e `docs/ddd/linguagem-ubiqua.md`).
<!-- TODO: Documentar explicitamente os comandos e atores do domínio e mapear os conceitos DDD documentados para as classes/casos de uso implementados. -->
- [ ] Relação entre documentação e implementação.

## 7. Funcionalidades Obrigatórias

### Criação da Ordem de Serviço
- [x] Identificação do cliente por CPF/CNPJ.
- [x] Cadastro de veículo (placa, marca, modelo e ano).
- [x] Inclusão de serviços.
- [x] Inclusão de peças e insumos.
- [x] Geração automática do orçamento.
- [x] Disponibilização do orçamento pela API.
- [x] Aprovação ou rejeição do orçamento.

### Acompanhamento da Ordem de Serviço
- [x] Status Recebida.
- [x] Status Em diagnóstico.
- [x] Status Aguardando aprovação.
- [x] Status Em execução.
- [x] Status Finalizada.
- [x] Status Entregue.
- [x] Controle de transições de status.
- [x] Regras de transição implementadas.
- [x] Consulta do andamento via API.

### Gestão Administrativa
- [x] CRUD de clientes.
- [x] CRUD de veículos.
- [x] CRUD de serviços.
- [x] CRUD de peças e insumos.
- [x] Controle de estoque.
- [x] Listagem de Ordens de Serviço.
- [x] Detalhamento de Ordens de Serviço.
- [x] Tempo médio de execução dos serviços.

## 8. Requisitos Técnicos Obrigatórios
- [x] Back-end monolítico.
- [x] Arquitetura em camadas.
- [x] Banco de dados com justificativa documentada.
- [x] APIs RESTful.
- [x] Swagger/OpenAPI integrado (em produção fica desabilitado por padrão e depende de configuração para habilitar).
- [x] Autenticação JWT.
- [x] Validação de CPF/CNPJ.
- [x] Validação de placa de veículo.
- [x] Testes unitários.
- [x] Testes de integração.
- [x] Cobertura mínima de 80% nos domínios críticos (95,91% em `domain.*` e 97,16% em `application.*` na execução registrada).
- [x] Instruções para execução local.
<!-- TODO: Revisar e corrigir a inconsistência transacional entre orçamento e OS já registrada em README.md; manter/expandir testes de regressão. -->
- [ ] Tratamento adequado de transações.

## 9. Evidências Recomendadas
- [x] Screenshot ou relatório de cobertura de testes (`docs/relatorio-cobertura/index.html`; resumo e execução em `docs/relatorio-cobertura/README.md`).
- [x] Relatório de análise de vulnerabilidades (`docs/dependency-check-report.html`).
<!-- TODO: Habilitar e validar o Swagger no ambiente de avaliação e guardar evidências verificáveis de Swagger, autenticação JWT e consulta de status da OS. -->
- [ ] Swagger funcionando e validado por evidência.
- [x] Dockerfile.
- [x] Docker Compose (`compose.yml`).
- [ ] Evidência de autenticação JWT.
- [ ] Evidência de consulta de status da OS.
