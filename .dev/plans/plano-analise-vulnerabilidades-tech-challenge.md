# Plano de Análise de Vulnerabilidades
## Tech Challenge - Fase 1

### Objetivo

Realizar uma análise de vulnerabilidades da solução desenvolvida para o Tech Challenge, identificando possíveis riscos de segurança relacionados ao código-fonte, dependências utilizadas e imagem Docker da aplicação. Ao final, deverá ser produzido um relatório contendo os resultados obtidos, as evidências coletadas e as recomendações de mitigação.

---

# Escopo da Análise

A análise abrangerá os seguintes componentes:

- API Spring Boot
- Dependências Maven
- Imagem Docker da aplicação
- Configurações de segurança
- Processo de autenticação baseado em JWT

Não fazem parte do escopo:

- Testes de intrusão (pentest)
- Ataques em ambiente produtivo
- Análise de infraestrutura em nuvem

---

# Estratégia de Execução

A análise será realizada utilizando ferramentas automatizadas integradas ao processo de desenvolvimento e complementada por verificações manuais.

## Etapa 1 - Análise de Dependências

### Objetivo

Identificar bibliotecas com vulnerabilidades conhecidas (CVEs).

### Ferramentas

- GitHub Dependabot
- OWASP Dependency Check

### Atividades

- Configurar Dependabot no repositório.
- Executar análise das dependências Maven.
- Verificar vulnerabilidades classificadas como Critical, High, Medium e Low.

### Evidências

- Capturas de tela dos alertas do Dependabot.
- Relatório HTML gerado pelo OWASP Dependency Check.

---

# Etapa 2 - Análise da Imagem Docker

### Ferramenta

- Trivy

### Atividades

- Executar análise da imagem Docker gerada pelo projeto.
- Verificar vulnerabilidades do sistema operacional base.
- Verificar vulnerabilidades de bibliotecas embarcadas.

---

# Etapa 3 - Revisão Manual de Segurança

### Itens de Verificação

- JWT implementado corretamente.
- Endpoints administrativos protegidos.
- Uso de BCrypt.
- Ausência de senhas hardcoded.
- Uso de consultas parametrizadas.
- Variáveis sensíveis externalizadas.
- Logs sem exposição de credenciais.

---

# Etapa 4 - Automação no GitHub

```text
Push / Pull Request
        ↓
Build Maven
        ↓
Testes Unitários
        ↓
OWASP Dependency Check
        ↓
Trivy
        ↓
Publicação dos Relatórios
```

---

# Relatório Final de Vulnerabilidades

## Estrutura

1. Introdução
2. Ferramentas Utilizadas
3. Ambiente Avaliado
4. Resultados
5. Vulnerabilidades Encontradas
6. Evidências
7. Conclusão

---

# Cronograma Sugerido

- Dia 1: Dependabot + OWASP
- Dia 2: Trivy e correções
- Dia 3: Revisão manual
- Dia 4: Consolidação do relatório

---

# Critério de Conclusão

- [ ] Dependabot configurado
- [ ] Análise OWASP executada
- [ ] Análise Trivy executada
- [ ] Revisão manual realizada
- [ ] Evidências coletadas
- [ ] Relatório final produzido
- [ ] Vulnerabilidades críticas tratadas ou justificadas
