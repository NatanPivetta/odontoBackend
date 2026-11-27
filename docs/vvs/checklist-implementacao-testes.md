# Checklist de implementação dos testes do OdontoUFRGS

Atualizado em 24/11/2026. Acompanha a implementação na branch `develop` do backend e a execução dos cenários definidos no [plano de testes](plano-de-teste.md). O [índice de evidências](evidencias/README.md) registra a execução inicial.

## Concluído

- [x] Configurar Surefire e Failsafe. A fase `test` roda sem banco. O Failsafe executa os `*IT` no ciclo de `verify`.
- [x] Gerar relatórios JaCoCo em HTML e XML.
- [x] Integrar o PMD ao `verify` com bloqueio por violações. A execução inicial após a remoção de um método privado sem uso registrou zero violações.
- [x] Criar o perfil `test` e migrar o teste de inicialização para `OdontoApplicationIT`, com PostgreSQL 16 descartável e SMTP simulado.
- [x] Corrigir cinco asserções de `AtividadeServiceTest` para verificar códigos de erro do domínio. A suíte inicial passou com 35 testes.
- [x] Adicionar 49 testes unitários a oito services. Cobertura de autenticação, JWT, envio de e-mail, matrícula, feedback e gestão de usuários, professores e alunos. A suíte anterior à etapa HTTP passou com 84 testes.
- [x] Adicionar 15 testes HTTP em `ResourceHttpTest` para autenticação, turmas, atividades e feedback. O MockMvc executa a configuração de segurança, a validação e o tratamento de exceções da aplicação, com os services de negócio simulados.
- [x] Validar a suíte rápida com `.\mvnw.cmd -B -ntp test` no diretório `backend`. Resultado de 99 aprovados, zero falhas, zero erros e zero ignorados.

- [x] Adicionar 20 testes unitários para alta, atividades OUTROS, datas de conclusão, edição e operações em lote de turmas e normalização de cartão.
- [x] Adicionar 16 testes HTTP administrativos para usuários, professores e alunos. Os cenários cobrem cadastro, edição, desativação, paginação, filtros, validação, conflito e permissões.
- [x] Separar a execução dos testes HTTP da medição unitária do JaCoCo e habilitar o bloqueio das metas no `verify`.
- [x] Validar `.\mvnw.cmd -B -ntp clean verify '-DskipITs'` em `backend`. Passaram 104 testes unitários, 31 testes de Resource, PMD e JaCoCo.
- [x] Confirmar que o JaCoCo retorna código 1 ao avaliar a cobertura abaixo das metas com apenas os testes do normalizador.

- [x] Atualizar o Testcontainers para 1.21.4, compatível com o Docker Engine 29.9.3, e desativar a checagem de SMTP do Actuator apenas no perfil de teste, que simula o transporte.
- [x] Validar `.\mvnw.cmd -B -ntp verify` com Docker. Passaram 104 testes unitários, 31 testes de Resource, 1 teste de integração, PMD e JaCoCo.
- [x] Validar a parte de inicialização e migrations do C07. O Liquibase aplicou 37 changesets ao PostgreSQL 16 descartável, o Hibernate validou o schema e o contexto iniciou.


## Cobertura global do backend

| Etapa | Linhas | Ramificações |
| --- | ---: | ---: |
| 35 testes iniciais | 30,5% | 28,3% |
| 84 testes após a ampliação da suíte unitária | 54,4% | 49,6% |
| 99 testes, incluindo os 15 casos HTTP | 65,5% (582/888) | 53,7% (131/244) |
| 104 testes unitários em execução separada | 58,2% (517/888) | 66,8% (163/244) |

Relatórios locais em [Surefire](../../backend/target/surefire-reports/) e [JaCoCo](../../backend/target/site/jacoco/index.html). A medição da etapa de 99 testes incluía MockMvc. Na última execução, os 31 testes HTTP rodaram separadamente e a cobertura reflete os 104 unitários.

## Recorte das metas JaCoCo

O `verify` aplica as metas em conjunto a `AtividadeService`, `FeedbackService`, `TurmaService`, `AuthService`, `AlunoService`, `ProfessorService`, `UserService`, `CardNumberNormalizer` e `JwtService`. O recorte concentra as regras de negócio, a normalização e o JWT. Resources, DTOs, entidades, mappers, repositórios e infraestrutura ficam no relatório global. `EmailService` tem testes próprios de transporte SMTP.

| Indicador no recorte | Meta | Medido |
| --- | ---: | ---: |
| Linhas | 80% | 90,8% (357/393) |
| Ramificações | 70% | 81,8% (162/198) |

## Cenários HTTP já verificados

- [x] Autenticação (parte de C01 e C07). Login `200`, credenciais inválidas `401`, envio de código `204`, cadastro `201`, código expirado `409` e campos inválidos `422`. Verificações de ausência de senha, hash ou token nos casos pertinentes.
- [x] Segurança HTTP (parte de C02). Sem sessão e com Bearer inválido recebem `401`. JWT válido com autoridade de consulta permite listar turmas. A mesma autoridade é insuficiente para matricular, com retorno `403`.
- [x] Turmas, atividades e feedback (partes de C03 a C07). Encaminhamento de paginação e ordenação, conflito de matrícula `409`, atividade ocultada pelo serviço `404`, exclusão `204`, criação de feedback `201` e texto inválido `422`.

- [x] Administração. Validação de papel obrigatório, usuário inexistente, conflito de e-mail, paginação, filtros, desativação e permissões.

## Integração com PostgreSQL

- [x] C01, primeiro acesso e login. O `OdontoApplicationIT` verificou o envio do código com SMTP simulado, a persistência e o consumo do código, o armazenamento da senha como hash e o login após o cadastro. O Failsafe aprovou 2 testes, incluindo o de inicialização, sem falhas, erros ou casos ignorados. [Relatório Failsafe](../../backend/target/failsafe-reports/br.ufrgs.odonto.OdontoApplicationIT.txt)
- [x] C01, casos negativos. A API recusou senha incorreta com `401` e códigos expirado e incorreto com `409`. As recusas ficaram sem token e, nos casos de cadastro, sem criar aluno ou consumir o código.
- [x] C07, campos obrigatórios de cadastro. A API retornou `422` com os erros de senha e código ausentes e o banco ficou sem o novo usuário. A execução do Failsafe em 27/11/2026 aprovou os 6 cenários de `OdontoApplicationIT`. [Relatório Failsafe](../../backend/target/failsafe-reports/br.ufrgs.odonto.OdontoApplicationIT.txt)

- [x] C04, matrículas e rollback. A API confirmou matrícula, conflito por duplicidade, transferência, remoção e rematrícula. Uma restrição temporária no PostgreSQL descartável recusou o vínculo de destino no lote. A matrícula de origem permaneceu ativa. O Failsafe aprovou os 8 testes de integração. [Relatório Failsafe](../../backend/target/failsafe-reports/br.ufrgs.odonto.OdontoApplicationIT.txt)

- [x] C05, consultas de repositório. O PostgreSQL confirmou filtros combinados por aluno e turma, exclusão de atividades inativas, ordenação, paginação, contagem e resultado vazio. As consultas de turma exigiram turma e vínculo ativos. O Failsafe aprovou os 10 testes de integração. [Relatório Failsafe](../../backend/target/failsafe-reports/br.ufrgs.odonto.OdontoApplicationIT.txt)

- [x] C02, JWT e permissões. A API retornou `401` sem token ou com token malformado. O aluno com JWT do login real consultou a turma com `200` e recebeu `403` ao tentar matricular. O PostgreSQL confirmou a ausência do vínculo. O Failsafe aprovou 11 testes. [Relatório Failsafe](../../backend/target/failsafe-reports/br.ufrgs.odonto.OdontoApplicationIT.txt)

- [x] C03, ciclo da atividade. O aluno criou, consultou e editou a própria atividade. Outro aluno recebeu `404` ao consultar ou editar. O professor excluiu a atividade logicamente e o banco manteve o registro com `active = false`. O Failsafe aprovou 12 testes. [Relatório Failsafe](../../backend/target/failsafe-reports/br.ufrgs.odonto.OdontoApplicationIT.txt)

- [x] C06, alta e feedback. O aluno recebeu `409` ao tentar conceder alta, sem alteração no banco. O professor concedeu alta e publicou feedback, ambos persistidos. O titular leu o feedback e outro aluno recebeu `404`.
- [x] C07, validação de atividades. Campo obrigatório ausente retornou `422`, enum inválido retornou `400` e ID inexistente retornou `404`. O handler agora responde com `400` para corpos que não podem ser lidos.
- [x] Validar o backend com `verify` em 27/11/2026. Passaram 104 unitários, 31 testes de Resource e 14 de integração, além de PMD e das metas JaCoCo. [Relatório Failsafe](../../backend/target/failsafe-reports/br.ufrgs.odonto.OdontoApplicationIT.txt)

## Testes de sistema

- [x] S01, autenticação no navegador. Quatro testes aprovaram redirecionamento sem sessão, senha incorreta e login e logout de aluno e professor.
- [x] S02, turmas e matrículas. O professor criou a turma, matriculou o aluno, verificou o bloqueio de matrícula duplicada, removeu e rematriculou. A matrícula apareceu após recarregar a página.
- [x] Executar S01 e S02 com Chromium visível e atraso de 500 ms. A execução aprovou os cinco testes com API real, PostgreSQL dedicado e contas sintéticas. [Relatório HTML](../../odontoufrgs/playwright-report/index.html) · [JUnit XML](../../odontoufrgs/test-results/junit.xml)
- [x] Executar o ESLint no frontend inteiro. A execução encontrou 17 erros e 3 avisos no código da aplicação.
- [x] Implementar e executar S03 a S07 com API real, PostgreSQL dedicado e Mailpit. A última rodada do Chromium teve 10 aprovados e 1 falha. O ESLint passou nos arquivos do Playwright. [Relatório HTML](../../odontoufrgs/playwright-report/index.html)
- [x] S04, alta. O professor concluiu a atividade e registrou a alta, que permaneceu após recarregar. O aluno pôde consultar a atividade sem a ação de registrar alta.
- [x] S05, feedback. O professor publicou o feedback, o titular conseguiu ler e outro aluno recebeu a mensagem de atividade indisponível.
- [x] S06, consultas. Os filtros de status mostraram resultado vazio e a atividade esperada. O detalhe abriu e a interface exibiu erro para um ID inexistente.
- [x] S07, primeiro acesso. O Mailpit recebeu o código. A interface recusou código incorreto e expirado e permitiu concluir o cadastro e fazer login após informar o código válido.
- [x] S03, registro e edição de atividade. A consulta de orientadores passou a fornecer ID e nome ao aluno autorizado. O Chromium confirmou o bloqueio sem turma, a descrição obrigatória para `OUTROS`, a recusa da data futura e a persistência da edição após recarregar.
- [x] Executar duas rodadas completas com o Compose de E2E reinicializado. Cada rodada aprovou 11 testes. A segunda gravou 11 vídeos. [Evidências](evidencias/README.md)
- [x] Triar os achados do ESLint. Os 17 erros e 3 avisos do código da aplicação foram registrados por regra. O PMD segue aprovado e integrado ao build. [Triagem](evidencias/README.md#triagem-de-análise-estática)
- [x] Consolidar a matriz de rastreabilidade e arquivar os relatórios e vídeos. [Evidências da execução](evidencias/execucoes-2026-11-27/)

## Pendente

- [ ] Publicar a documentação e as evidências no repositório de entrega junto com os commits do backend e do frontend.
