# Evidência de testes e cobertura

Relatório JaCoCo HTML gerado por `.\mvnw.cmd clean verify` em 28/10/2026, com JDK 21.0.2.

| Resultado | Valor |
|---|---:|
| Testes executados | 175 |
| Falhas, erros ou ignorados | 0 |
| Cobertura de instruções em `domain.*` | 95,91% |
| Cobertura de instruções em `application.*` | 97,16% |
| Gates JaCoCo de 80% | Aprovados |

Abra [`index.html`](index.html) para consultar o relatório completo por pacote e classe. Os arquivos HTML, CSS e JavaScript desta pasta compõem o relatório offline, sem depender de `target/`.

Para gerar novamente o relatório e atualizar esta evidência:

```powershell
.\mvnw.cmd clean verify
```

Após validar a execução, copie o conteúdo de `target/site/jacoco/` para `docs/relatorio-cobertura/`. Os percentuais apresentados correspondem somente à execução registrada acima e não incluem `infrastructure` nem `presentation`.
