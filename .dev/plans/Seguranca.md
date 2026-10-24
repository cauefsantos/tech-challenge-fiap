# Guia de Segurança para Desenvolvedores

Este documento descreve como configurar o ambiente local para execução das análises de vulnerabilidades e verificações de segurança utilizadas pelo projeto.

---

# Pré-requisitos

Instalar:

- Java 21 ou superior
- Maven
- Docker Desktop (ou Docker Engine)
- Git

Verificações:

```bash
java -version
mvn -version
docker --version
git --version
```

---

# Configuração da NVD API Key

O OWASP Dependency Check utiliza a base de vulnerabilidades da NVD (National Vulnerability Database). Para melhorar o desempenho e evitar limitações de acesso, recomenda-se utilizar uma API Key.

## Solicitar a API Key

A chave pode ser obtida gratuitamente no portal da NVD:

```text
https://nvd.nist.gov/developers/request-an-api-key
```

Após o cadastro, a chave será enviada por e-mail.

---

# Configuração no Windows

## Configuração temporária (somente sessão atual)

PowerShell:

```powershell
$env:NVD_API_KEY="sua_chave_aqui"
```

CMD:

```cmd
set NVD_API_KEY=sua_chave_aqui
```

Verificar:

```powershell
echo $env:NVD_API_KEY
```

---

## Configuração permanente

PowerShell ou CMD:

```cmd
setx NVD_API_KEY "sua_chave_aqui"
```

Após executar o comando:

1. Feche o terminal.
2. Abra um novo terminal.

Verifique:

```cmd
echo %NVD_API_KEY%
```

ou

```powershell
echo $env:NVD_API_KEY
```

---

# Configuração no Linux/macOS

Adicionar à sessão atual:

```bash
export NVD_API_KEY="sua_chave_aqui"
```

Verificar:

```bash
echo $NVD_API_KEY
```

Para tornar permanente, adicionar ao arquivo:

```bash
~/.bashrc
```

ou

```bash
~/.zshrc
```

Exemplo:

```bash
export NVD_API_KEY="sua_chave_aqui"
```

Aplicar:

```bash
source ~/.bashrc
```

---

# Execução do OWASP Dependency Check

## Execução Local

Após configurar a variável de ambiente:

```bash
mvn dependency-check:check
```

Arquivos gerados:

```text
target/
└── dependency-check-report.html
```

Abrir o relatório:

```text
target/dependency-check-report.html
```

---

# Interpretação do Relatório

Verificar:

- Vulnerabilidades críticas (Critical)
- Vulnerabilidades altas (High)
- Vulnerabilidades médias (Medium)
- Vulnerabilidades baixas (Low)

Itens analisados:

- Dependências Maven
- Bibliotecas transitivas
- Componentes conhecidos com CVEs

---

# Análise da Imagem Docker com Trivy

## Instalação

### Windows

```powershell
winget install AquaSecurity.Trivy
```

### Linux

```bash
sudo apt install trivy
```

ou conforme documentação da distribuição.

---

## Construção da Imagem

```bash
docker build -t tech-challenge .
```

---

## Execução da Análise

```bash
trivy image tech-challenge
```

---

## Gerar Relatório

```bash
trivy image \
  --format table \
  --output trivy-report.txt \
  tech-challenge
```

Arquivo gerado:

```text
trivy-report.txt
```

---

# Verificação de Dependências Desatualizadas

Executar:

```bash
mvn versions:display-dependency-updates
```

Objetivo:

- Identificar dependências antigas.
- Verificar versões mais recentes.
- Avaliar atualizações de segurança.

---

# Verificação de Plugins Maven

Executar:

```bash
mvn versions:display-plugin-updates
```

Objetivo:

- Identificar plugins desatualizados.
- Manter ferramentas de build atualizadas.

---

# Verificações Manuais de Segurança

Antes de cada entrega, verificar:

## Autenticação

- JWT configurado.
- Endpoints administrativos protegidos.
- Recursos públicos claramente definidos.

## Senhas

- BCrypt utilizado.
- Nenhuma senha armazenada em texto puro.
- Nenhuma senha versionada no Git.

## Banco de Dados

- Uso de JPA.
- Uso de consultas parametrizadas.
- Ausência de SQL concatenado.

## Configuração

- Credenciais em variáveis de ambiente.
- Ausência de segredos no código-fonte.
- Perfis de ambiente separados (dev/prod).

## Logs

- Não registrar senhas.
- Não registrar tokens JWT.
- Não registrar dados sensíveis dos usuários.

---

# Checklist de Segurança Local

## Dependências

- [ ] Dependency Check executado
- [ ] Nenhuma vulnerabilidade crítica encontrada
- [ ] Dependências atualizadas

## Docker

- [ ] Trivy executado
- [ ] Nenhuma vulnerabilidade crítica encontrada
- [ ] Imagem reconstruída após atualizações

## Aplicação

- [ ] JWT validado
- [ ] BCrypt validado
- [ ] Variáveis de ambiente configuradas
- [ ] Sem segredos no repositório

## Entrega

- [ ] Relatório OWASP gerado
- [ ] Relatório Trivy gerado
- [ ] Evidências anexadas à documentação

---

# Arquivos de Evidência Esperados

```text
target/dependency-check-report.html
```

```text
trivy-report.txt
```

```text
security-report.md
```

Esses artefatos constituem as evidências mínimas recomendadas para demonstrar a análise de vulnerabilidades da aplicação durante a entrega do projeto.