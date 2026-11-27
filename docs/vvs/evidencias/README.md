# Evidências de teste do OdontoUFRGS

## Validação de 27/11/2026

A execução de `verify` no backend aprovou 104 testes unitários, 31 de Resource e 14 de integração. O PMD e as metas JaCoCo passaram. Os testes de sistema usam Chromium, Playwright 1.58.2, API Spring Boot, PostgreSQL 16 e Mailpit no Compose de E2E.

As duas rodadas completas aprovaram 11 testes cada, com o Compose reinicializado entre elas. A segunda preservou 11 vídeos do contexto principal. As verificações com contextos adicionais aparecem no resultado do teste. [Rodada 1 em XML](execucoes-2026-11-27/playwright-rodada-1.xml) · [Rodada 2 em XML](execucoes-2026-11-27/playwright-rodada-2.xml) · [Relatório HTML com vídeos](execucoes-2026-11-27/playwright/index.html)

Os relatórios do backend estão preservados nesta mesma pasta. [Surefire](execucoes-2026-11-27/surefire-resumo.txt) · [Failsafe](execucoes-2026-11-27/br.ufrgs.odonto.OdontoApplicationIT.txt) · [JaCoCo](execucoes-2026-11-27/jacoco.xml) · [PMD](execucoes-2026-11-27/pmd.xml)

Para reproduzir, rode `.\mvnw.cmd verify` em `backend` com JDK 21 e Docker disponível. Com os checkouts `backend` e `odontoufrgs` lado a lado, rode `npm ci` e `npx playwright install chromium` no frontend. A partir da pasta que contém os dois checkouts, os comandos abaixo reinicializam somente o ambiente descartável e executam a suíte.

```powershell
docker compose --file backend/compose.e2e.yml down
.\odontoufrgs\e2e\run-auth.ps1 -Spec '.spec.ts'
docker compose --file backend/compose.e2e.yml down
.\odontoufrgs\e2e\run-auth.ps1 -Video -Spec '.spec.ts'
```

| Cenário | Implementação | Evidência verificada |
| --- | --- | --- |
| C01 | `OdontoApplicationIT` | Cadastro, login, senha inválida e códigos recusados |
| C02 | `OdontoApplicationIT` | JWT, permissões e ausência de vínculo após operação recusada |
| C03 | `OdontoApplicationIT` | Criação, edição, exclusão lógica e isolamento por titular |
| C04 | `OdontoApplicationIT` | Matrículas, transferência e rollback no PostgreSQL |
| C05 | `OdontoApplicationIT` | Consultas, paginação, filtros e vínculos ativos |
| C06 | `OdontoApplicationIT` | Alta e feedback com persistência |
| C07 | `OdontoApplicationIT` | Migrations, schema e validação da API |
| S01 | `e2e/auth.spec.ts` | Sessão, login, logout e senha incorreta |
| S02 | `e2e/turmas.spec.ts` | Criação de turma, matrícula, duplicidade, remoção e rematrícula |
| S03 e S04 | `e2e/atividades.spec.ts` | Registro, edição, validações e alta |
| S05 | `e2e/feedback.spec.ts` | Publicação e visibilidade pelo titular |
| S06 | `e2e/atividades.spec.ts` | Filtros, resultado vazio, detalhe e atividade ausente |
| S07 | `e2e/primeiro-acesso.spec.ts` | Código do Mailpit, cadastro, login, código incorreto e expiração |

O S03 revelou que o perfil de aluno recebia `403` ao carregar professores. A correção cria uma consulta de orientadores que retorna somente ID e nome e mantém as permissões da listagem administrativa.

### Triagem de análise estática

O PMD é a ferramenta de análise estática integrada ao build. O ESLint passou nos arquivos do Playwright. A configuração do ESLint ignora os relatórios gerados e a execução sobre o código do frontend registrou 17 erros e 3 avisos preexistentes.

| Regra | Achados | Decisão |
| --- | ---: | --- |
| `@typescript-eslint/no-explicit-any` | 14 erros | Revisar tipos de componentes e tratamento de exceções em manutenção do frontend. |
| `react-hooks/set-state-in-effect` | 3 erros | Revisar separadamente os ciclos de autenticação, filtro e combobox. Alterar essa lógica requer regressão de interface adicional. |
| `@typescript-eslint/no-unused-expressions` | 2 avisos | Revisar expressões condicionais de seleção na tela de matrículas. |
| `@typescript-eslint/no-unused-vars` | 1 aviso | Remover importação sem uso no modal de atividade em manutenção do frontend. |

## Execução inicial do backend

Execução local de 24/11/2026, sobre o commit base `9608169` da `develop` com as alterações desta etapa ainda em edição. Ferramentas usadas incluem JDK 21.0.4, Maven Wrapper 3.9.14, Surefire e Failsafe 3.5.2, JaCoCo 0.8.13 e PMD 7.7.0 (plugin Maven 3.26.0).

Comandos a partir de `backend/`, com JDK 21 disponível.

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean verify
```

`clean test` roda a suíte unitária e gera o relatório JaCoCo. `clean verify` também executa PMD e Failsafe. Para reproduzir a análise estática isoladamente, execute `.\mvnw.cmd pmd:check`.

| Verificação | Resultado | Artefato local |
| --- | --- | --- |
| Surefire | 35 aprovados, 0 falhas, 0 erros, 0 ignorados na execução final | `backend/target/surefire-reports/` |
| JaCoCo unitário global | 271/888 linhas (30,5%) e 69/244 branches (28,3%) | `backend/target/site/jacoco/` |
| PMD | 0 violações após remover a sobrecarga privada sem uso de `AtividadeService.resolverStatus` | `backend/target/reports/pmd.html` e `backend/target/pmd.xml` |
| Failsafe | 1 erro de infraestrutura e 0 ignorados. O Docker não disponibilizou um daemon utilizável para o Testcontainers. | `backend/target/failsafe-reports/` |

| Cenário | Teste | Resultado |
| --- | --- | --- |
| U04, parcialmente | `AtividadeServiceTest` | 27 testes aprovados. Cinco asserções existentes passaram a verificar os códigos de erro do domínio. |
| C07, migrations e compatibilidade do schema | `OdontoApplicationIT` | Bloqueado antes da inicialização do PostgreSQL 16. A validação de entradas da API segue pendente. |

A verificação das metas de cobertura depende da definição do escopo de classes do JaCoCo Check. A expansão dos testes unitários e de componente, o ESLint e a suíte Playwright seguem pendentes nesta etapa.
