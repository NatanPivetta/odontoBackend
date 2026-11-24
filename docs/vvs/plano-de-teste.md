# Plano de teste e implementação — OdontoUFRGS

Data: 10/11/2026. Estado: planejamento; as atividades propostas abaixo ainda precisam ser implementadas e executadas. Responsável: estudante autor do trabalho individual.

Referência de avaliação: [enunciado trabalho final](<enunciado trabalho final>). A meta é atender ao nível **Excelente** dos cinco critérios obrigatórios (10,0 pontos). As metas de cobertura e os prazos deste plano são decisões do projeto, não exigências numéricas do enunciado.

## 1. Sistema e situação inicial

O OdontoUFRGS gerencia turmas, matrículas, atividades clínicas e feedback de professores para alunos. O backend usa Java 21, Spring Boot 3.4.4, Spring Security, JPA, Liquibase e PostgreSQL; o frontend usa Next.js 16, React 19 e TypeScript.

Inventário observado no repositório:

- `backend/pom.xml`: JUnit Jupiter, Mockito e AssertJ por meio de `spring-boot-starter-test`, além de Spring Security Test e Testcontainers (JUnit/PostgreSQL).
- `AtividadeServiceTest`: testes unitários existentes de criação, consulta, edição, datas e exclusão lógica. Devem ser executados e revisados antes de ampliar a suíte.
- `UserAuthoritiesTest` e `ResourceAuthorizationAnnotationsTest`: verificações de permissões e anotações. A inspeção de anotações não comprova autorização em requisições reais.
- `OdontoApplicationTests`: teste de carregamento de contexto. Não foi identificada configuração Testcontainers nessa classe; a simples presença da dependência não torna o banco de testes isolado.
- Não foram identificados plugins JaCoCo/PMD/Failsafe no POM nem dependência/configuração Playwright no frontend. Há script de ESLint, mas ainda é necessário executar e registrar seus resultados.

Este inventário é uma leitura dos arquivos, não uma evidência de testes aprovados. A documentação existente pode estar desatualizada: por exemplo, o README descreve cartão de nove dígitos, enquanto `CardNumberNormalizer` normaliza para oito. Resolver o contrato antes de definir os resultados esperados desses testes; não transformar comportamento divergente em requisito sem análise.

## 2. Rastreabilidade com a rubrica

| Critério | Peso | Implementação para nível Excelente | Evidência de conclusão |
| --- | ---: | --- | --- |
| Plano de teste | 1,5 | Manter escopo, estratégias, critérios, riscos, ferramentas e cronograma atualizados | Este documento e matriz de cenários com resultados |
| Análise estática | 1,5 | PMD integrado ao `verify`; executar ESLint como complemento; corrigir ou justificar os principais achados | Relatório PMD, saída do lint e registro de decisões |
| Unitários | 3,0 | JUnit 5/Mockito; sucesso, falha, exceção e limites nas regras principais; cobertura JaCoCo | Surefire, relatório JaCoCo e metas verificadas |
| Componentes | 2,5 | API e persistência isoladas de serviços externos; cenários de sucesso, erro e borda no build | Failsafe, resultados das verificações HTTP e banco |
| Sistema | 1,5 | Playwright com frontend, API e PostgreSQL reais; fluxos principais e alternativos | Relatório HTML/JUnit, vídeos e traces de falhas |
| Carga (opcional) | +0,5 | k6 com carga, duração, critérios e ambiente documentados | Script, resumo de execução e análise |
| Segurança (opcional) | +0,5 | Autenticação, autorização por objeto e exposição de dados | Casos reproduzíveis, requisições sanitizadas e achados |

Prioridade: concluir os cinco critérios obrigatórios antes dos extras. Unitários e componentes somam 5,5 pontos e recebem a maior parcela do esforço. Vídeos apoiam a evidência; cada teste de sistema também deve verificar resultados com assertions.

## 3. Escopo

Incluído:

- Login, sessão e primeiro acesso com código de verificação.
- Permissões de professor/aluno e acesso apenas aos dados autorizados.
- Criação e gestão de turmas, matrícula, remoção e rematrícula de alunos.
- Cadastro, consulta, filtros, edição e exclusão lógica de atividades.
- Regras de datas, descrição do tipo OUTROS, status e concessão de ALTA.
- Criação e leitura de feedback, incluindo a troca de usuário professor/aluno.
- Persistência, migrations e respostas de erro da API nesses fluxos.

Fora do escopo obrigatório: infraestrutura AWS/Terraform, disponibilidade de produção, OAuth futuro, entrega real de e-mail pelo Gmail, compatibilidade exaustiva entre navegadores, auditoria completa de segurança e desempenho. O Chromium será o navegador inicial. E-mail será validado com servidor SMTP local de teste, sem depender de contas reais.

## 4. Ambiente e isolamento

| Camada | Ferramentas e ambiente planejados |
| --- | --- |
| Estática Java | Maven Wrapper e PMD com versão fixada no POM |
| Estática frontend | ESLint já declarado no projeto |
| Unitários | JUnit 5, Mockito, AssertJ e Surefire |
| Cobertura | JaCoCo com relatório HTML/XML e verificação de metas |
| Componentes | Spring Boot Test, MockMvc, Spring Security Test, Testcontainers e PostgreSQL 16 |
| Sistema | Playwright Test/TypeScript, Chromium, Next.js, API e PostgreSQL 16 dedicados |
| E-mail | SMTP local capturável, como Mailpit, somente no ambiente de teste |
| Extras | k6 e verificações de segurança documentadas |

Usar JDK 21 e Node compatível com o Next.js instalado; fixar dependências e versionar os lockfiles. Docker é necessário para Testcontainers. Registrar versões efetivamente usadas no relatório de execução.

Criar perfil `test` para componentes e perfil `e2e` para sistema, com configurações explícitas de banco, SMTP, JWT e migrations. Evitar herdar configurações locais pessoais. Definir relógio controlável nas regras de tempo ou dados relativos ao dia da execução, mantendo o fuso America/Sao_Paulo.

Para componentes, iniciar PostgreSQL descartável via Testcontainers e aplicar Liquibase. Para sistema, usar banco/volume exclusivo de E2E e aplicar as mesmas migrations. Não apontar testes ao endereço de produção nem reutilizar o banco local de desenvolvimento.

Preparar dados sintéticos: professor com permissões administrativas, professor orientador, alunos A e B, duas turmas, matrícula ativa/inativa e atividades nos estados necessários. Cada execução deve ter identificador próprio. Preparação e limpeza devem ser idempotentes e restritas ao ambiente de teste. Não depender da ordem dos testes ou de IDs fixos do banco.

Não versionar senhas reais, JWTs, `storageState`, dados de pacientes ou arquivos de ambiente pessoais. Usar nomes e prontuários fictícios também nos vídeos. Sessões de aluno e professor devem usar contextos distintos.

## 5. Estratégia e catálogo de cenários

Aplicar particionamento de equivalência (papéis, estados, dados válidos/inválidos), valores de borda (datas, comprimentos e paginação), tabelas de decisão (ALTA e matrícula) e transições de estado. Para cada caso, registrar preparação, ação, resultado esperado e evidência. Os IDs abaixo devem aparecer nos nomes dos testes ou no relatório de rastreabilidade.

### 5.1 Análise estática

Integrar PMD ao ciclo `verify`, com geração de relatório e falha por violações das regras acordadas. Começar por regras de erros potenciais, código morto e práticas de implementação, sem exigir uma reformatação geral do projeto. Se houver achados preexistentes, triá-los antes de fixar o bloqueio; supressões devem ser pontuais e justificadas.

Executar também o ESLint do frontend. Registrar ferramenta/regra, arquivo, severidade, decisão, justificativa e commit da correção. Meta de saída: nenhum achado de maior severidade pendente sem justificativa e nenhuma supressão global para esconder resultados. Disponibilizar o relatório PMD e o comando capaz de reproduzi-lo.

### 5.2 Testes unitários

Isolar repositórios, envio de e-mail, geração/validação de token e outras dependências externas com mocks. Usar objetos reais para entidades e valores quando isso permitir verificar a regra. Não carregar Spring nem banco nesta suíte. Verificar também que operações inválidas não persistem alterações nem enviam e-mail indevidamente.

| ID | Unidade | Casos a implementar ou complementar |
| --- | --- | --- |
| U01 | `CardNumberNormalizer` | Null, vazio, pontuação, zeros à esquerda, comprimento menor/igual/maior que o contrato acordado; parametrizar entradas |
| U02 | `AuthService` | Login válido/inválido; cadastro válido; cartão/e-mail duplicados; código incorreto, usado e expirado; falha do envio de e-mail |
| U03 | `TurmaService` | Matrícula válida, duplicada, turma fechada, usuário sem papel de aluno, remoção, rematrícula e transferência preservando uma matrícula ativa |
| U04 | `AtividadeService` | Complementar suíte atual: aluno sem turma, entidades inexistentes, OUTROS sem descrição, ontem/hoje/amanhã, data de conclusão futura, filtros e exclusão lógica |
| U05 | `AtividadeService.updateStatus` | Professor concede ALTA apenas a CONCLUIDA; aluno não concede ALTA; impedir saída de ALTA; impedir alteração de outro aluno |
| U06 | `FeedbackService` | Criar/listar feedback; atividade inexistente; aluno de outra atividade; exclusão com vínculo incompatível |
| U07 | `UserService`/`ProfessorService` | Cadastro/edição válidos, duplicidades, usuário inexistente, papel inválido e inativação |
| U08 | Segurança/JWT | Preservar testes existentes e testar token válido, expirado e inválido, sem confundir anotação com enforcement HTTP |

Meta interna: pelo menos 80% de linhas e 70% de branches no conjunto de serviços de negócio, normalizador e código de JWT selecionado. Publicar também a cobertura global para transparência. Não inflar a meta com getters, código gerado por Lombok/MapStruct ou testes sem assertions úteis. Configurar o escopo exato do `jacoco:check` e justificar exclusões. A cobertura unitária deve ser identificável separadamente da de componentes.

### 5.3 Testes de componente

Organizar classes como `*IT`, executadas pelo Maven Failsafe nas fases `integration-test` e `verify`. Separar dos `*Test` executados pelo Surefire. Migrar ou configurar o teste de contexto existente para usar ambiente isolado e não exigir o banco pessoal ao executar unitários.

Usar dois limites de componente: (a) API com MockMvc, cadeia de segurança e serviços reais sobre PostgreSQL descartável; (b) repositórios com `@DataJpaTest` e PostgreSQL Testcontainers, sem substituição automática por banco em memória. Substituir apenas integrações externas, como SMTP, na suíte de API. Não mockar repositórios nos testes que pretendem comprovar persistência.

| ID | Componente | Resultado a verificar |
| --- | --- | --- |
| C01 | Autenticação/API | Login/cadastro válidos, credenciais inválidas, código expirado; status e corpo sem senha/hash/token indevido |
| C02 | Segurança HTTP | Sem token, token inválido e permissão insuficiente são recusados; usuário autorizado consegue operar; manter filtros habilitados |
| C03 | Atividades/API | Criar, buscar, editar e excluir logicamente; validar corpo, banco e tentativa de ler/editar objeto de outro aluno |
| C04 | Turma/matrícula | Matrícula, duplicidade, remoção/rematrícula e transferência; verificar vínculos persistidos e rollback em operação em lote inválida |
| C05 | Repositórios | Filtros combinados, lista vazia, paginação/ordenação, registros inativos e consultas de matrícula ativa |
| C06 | Status/feedback | ALTA válida/inválida; feedback persistido e visível apenas a quem tem acesso; erro sem alteração parcial |
| C07 | Validação e migrations | Campos obrigatórios, IDs ausentes, enums inválidos; migrations em banco vazio e compatibilidade com entidades |

Assertions devem cobrir HTTP, payload, estado persistido e ausência de efeitos em falhas. Usar 400 para validação estrutural e códigos de negócio conforme o contrato consolidado com o handler; não assumir que toda falha de negócio é 400. Para recursos de outro aluno, respeitar o contrato de ocultação por 404 onde adotado. Limpar o banco entre cenários por rollback quando aplicável, ou por fixture explícita; chamadas HTTP reais não participam automaticamente da transação do teste.

### 5.4 Testes de sistema com Playwright

Executar navegador contra frontend e API reais no ambiente E2E. Preparar dados por fixture/API ou seed restrito ao perfil de teste; não interceptar respostas de negócio para substituir o backend nesses cenários. Usar locators por papel, rótulo e texto acessível; adicionar `data-testid` apenas quando necessário. Usar auto-wait e assertions do Playwright, evitando esperas fixas.

| ID | Fluxo principal | Alternativo/erro | Evidência funcional |
| --- | --- | --- | --- |
| S01 | Login e logout de professor/aluno | Senha incorreta e acesso a rota protegida sem sessão | Dashboard correto; erro no login; retorno ao login após sair |
| S02 | Professor cria turma e matricula aluno | Matrícula duplicada; remover e rematricular | Lista atualizada e vínculo preservado após recarregar |
| S03 | Aluno registra e edita atividade | Sem turma ativa; OUTROS sem descrição; data inválida | Detalhe persistido e mensagens de validação |
| S04 | Professor acompanha atividade e concede ALTA | Aluno tenta ALTA; tentativa de sair de ALTA | Estado final persistido e bloqueios observáveis |
| S05 | Professor envia feedback; aluno consulta | Aluno B tenta acessar atividade do aluno A | Comentário visível ao aluno A; dados protegidos para B |
| S06 | Consulta com filtros e detalhe | Filtro sem resultados e item inativo/inexistente | Resultados corretos, estado vazio e erro compreensível |
| S07 | Primeiro acesso com e-mail capturado localmente | Código incorreto/expirado | Conta criada e login posterior; erro sem cadastro indevido |

Priorizar vídeos completos de S02, S03 e S05; manter S01 e S04 na mesma seleção para demonstrar perfis e regras. Marcar os testes escolhidos com `@video`. Fluxos que alternam perfis podem gerar um arquivo por contexto: associar ambos ao mesmo caso no relatório e fechar explicitamente contextos criados manualmente.

## 6. Implementação planejada do Playwright e vídeos

Arquivos a criar em `odontoufrgs/`:

```text
playwright.config.ts
e2e/fixtures/          # seed, usuários e sessões isoladas
e2e/auth.spec.ts
e2e/turmas.spec.ts
e2e/atividades.spec.ts
e2e/feedback.spec.ts
e2e/primeiro-acesso.spec.ts
```

Instalar `@playwright/test` como dependência de desenvolvimento e o Chromium. Adicionar scripts `test:e2e`, `test:e2e:videos` e `test:e2e:report` ao `package.json`. Exemplo de configuração a implementar:

```ts
import { defineConfig, devices } from '@playwright/test';

const recording = process.env.E2E_VIDEO === '1';

export default defineConfig({
  testDir: './e2e',
  outputDir: 'test-results',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
  ],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:3000',
    viewport: { width: 1280, height: 720 },
    video: { mode: recording ? 'on' : 'retain-on-failure',
             size: { width: 1280, height: 720 } },
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
  projects: [{
    name: 'chromium',
    use: { ...devices['Desktop Chrome'], viewport: { width: 1280, height: 720 } },
  }],
});
```

Iniciar com um worker para simplificar o isolamento; aumentar o paralelismo apenas com dados independentes por worker. Configurar inicialização/readiness de frontend e API antes da suíte e propagar `NEXT_PUBLIC_API_URL` para o processo Next.js. O exemplo acima pressupõe os servidores já disponíveis; ele não os inicia.

Usar `video: 'on'` na execução de entrega para preservar também fluxos aprovados. `retain-on-failure` preserva apenas falhas e, sozinho, não atende à gravação das demonstrações de sucesso. Os arquivos são gravados ao fechar o contexto e ficam no diretório de resultados. Fonte: [documentação de vídeos do Playwright](https://playwright.dev/docs/videos).

Comandos planejados em PowerShell, após instalar/configurar a suíte e preparar o ambiente E2E:

```powershell
Set-Location C:\odontoUFRGS\odontoufrgs
npm install -D @playwright/test
npx playwright install chromium
npx playwright test --project=chromium
$env:E2E_VIDEO = '1'
try {
  npx playwright test --project=chromium --grep '@video'
} finally {
  Remove-Item Env:E2E_VIDEO
}
npx playwright show-report
```

Organizar ações com `test.step` para facilitar leitura do relatório. O vídeo registra a interação da página; o relatório e o trace explicam as verificações e as falhas. `codegen` pode ajudar a rascunhar seletores, mas o resultado precisa de assertions, dados isolados e revisão. Referências: [configuração](https://playwright.dev/docs/test-configuration) e [relatórios](https://playwright.dev/docs/test-reporters).

## 7. Critérios de entrada e saída

Entrada para implementação: revisar este escopo, resolver os contratos divergentes (cartão, erros HTTP e matrícula), confirmar ferramentas e criar fixtures sintéticas. Antes de executar componentes/sistema: banco isolado acessível, migrations aprovadas, SMTP de teste disponível, usuários preparados e aplicação pronta para receber requisições.

Saída obrigatória:

- Todos os cenários obrigatórios U/C/S têm implementação, resultado e vínculo com evidência; nenhum teste crítico está desabilitado para conseguir build verde.
- `verify` executa análise estática, unitários, componentes e cobertura; falha de teste ou meta deve resultar em código de saída não zero.
- Metas de cobertura atingidas, com sucesso, falha e exceção nos principais serviços; lacunas justificadas no relatório.
- Fluxos Playwright principais e alternativos aprovados em duas execuções consecutivas com dados reinicializados; isso é uma meta de estabilidade do projeto.
- Nenhum defeito crítico/alto aberto nos fluxos selecionados. Demais limitações têm impacto, evidência e decisão registrados, sem serem contadas como testes aprovados.
- Relatórios e vídeos selecionados acessíveis ao avaliador, com instruções de reprodução a partir de checkout limpo.
- Plano atualizado e PDF da Etapa 1 conferido/entregue. A rubrica avalia artefatos, sem apresentação final.

## 8. Riscos e mitigação

| Risco | Mitigação |
| --- | --- |
| README/backlog divergirem da implementação | Consolidar contrato antes de assertions; registrar decisão e regressões |
| Rematrícula ou conflito HTTP ter regressão | Priorizar U03/C04/S02 e verificar erro na interface e estado persistido |
| Banco local/produção receber dados de teste | Perfis explícitos, banco dedicado e validação do destino antes do seed/limpeza |
| SMTP externo tornar testes instáveis | Mock em unitários/componentes; servidor capturável local no E2E |
| Docker indisponível | Verificação prévia; registrar bloqueio, sem pular testes e anunciar sucesso |
| Datas, sessões ou dados compartilhados gerarem intermitência | Relógio controlado, contextos separados e fixtures independentes |
| Muitos achados estáticos consumirem o prazo | Executar cedo, priorizar severidade e documentar supressões pontuais |
| Vídeos não serem preservados ou ficarem ilegíveis | Modo `on` para seleção, viewport definido, fechamento de contextos e conferência dos arquivos |
| Artefatos conterem dados sensíveis ou ficarem inacessíveis | Dados fictícios, revisão dos anexos e cópia dos selecionados para entrega persistente |

## 9. Cronograma de implementação

Dias úteis relativos ao início da implementação; ajustar à data oficial da disciplina, ainda não informada. Estimativa de 10 dias, não promessa de prazo.

| Período | Atividade | Entrega verificável/dependência |
| --- | --- | --- |
| D1 | Conferir PDF, contratos, testes existentes e fixtures | Inventário executado, decisões e plano revisado |
| D2 | Perfis isolados, PMD, JaCoCo e separação Surefire/Failsafe | Primeiro `verify` reproduzível, com achados triados |
| D3–D4 | U01–U08, priorizando atividades, matrícula e autenticação | Unitários aprovados e cobertura mensurada |
| D5–D6 | C01–C07 com PostgreSQL real | Componentes aprovados no build e migrations verificadas |
| D7 | Playwright, preparação de dados e S01–S03 | Ambiente E2E reproduzível e primeiros vídeos |
| D8 | S04–S07, negativos e troca de perfil | Principais e alternativos com assertions |
| D9 | Correções e execução consolidada | Duas execuções E2E estáveis e relatórios consolidados |
| D10 | Gravação final e revisão da entrega | Vídeos, índice de evidências e reprodução limpa |
| Após obrigatórios | Extras de carga/segurança | Scripts, resultados e análise documentada |

## 10. Build, evidências e entrega

Após implementar os plugins e perfis, disponibilizar estes comandos, também na automação de CI caso adotada:

```powershell
# No backend: unitários rápidos; depois validação completa com Docker disponível.
Set-Location C:\odontoUFRGS\backend
.\mvnw.cmd test
.\mvnw.cmd verify

# No frontend: dependências fixadas, lint, build e suíte com ambiente E2E pronto.
Set-Location C:\odontoUFRGS\odontoufrgs
npm ci
npm run lint
npm run build
npx playwright test --project=chromium
```

Esses comandos descrevem o resultado esperado da implementação; este plano não afirma que a configuração atual já os atende. Separar o relatório JaCoCo unitário do integrado ou identificá-los explicitamente. Coletar Surefire (`backend/target/surefire-reports/`), Failsafe (`backend/target/failsafe-reports/`), JaCoCo e PMD nos caminhos definidos pelos plugins, além de `odontoufrgs/playwright-report/` e `odontoufrgs/test-results/`.

Criar `docs/vvs/evidencias/README.md` como índice com commit, data, versões, comandos, totais aprovados/falhos/ignorados, cobertura, achados estáticos e links de artefatos. Incluir uma matriz final `ID do cenário → teste → resultado → evidência`, lista de defeitos corrigidos e limitações. Cada evidência deve corresponder ao código entregue.

Não depender somente de artefatos temporários de CI: preservar a seleção final de relatórios e vídeos no repositório ou em release vinculada ao commit entregue. Para relatório HTML Playwright, preservar também os anexos/diretórios referenciados, não apenas `index.html`. Manter arquivos gerados de execuções rotineiras fora do controle de versão e publicar deliberadamente a seleção final, sanitizada.

## 11. Pontuação extra opcional

**Carga (+0,5):** implementar script k6 com login de usuários sintéticos e leitura/listagem de atividades no ambiente isolado. Proposta inicial: aquecimento de 30 s, patamares de 1, 10 e 25 usuários virtuais por 1 min e desaceleração de 30 s. Medir taxa de erro, vazão e p95; metas iniciais de erro <1% e p95 <1 s, identificadas como metas do projeto. Registrar hardware, volume de dados, duração, resultados e gargalos. Distinguir erros esperados de cenários negativos de falhas da carga válida.

**Segurança (+0,5):** documentar token ausente/adulterado/expirado, acesso cruzado entre alunos, tentativa de operações administrativas por aluno, saída de senha/hash em respostas e entradas maliciosas em texto. Verificar efeitos reais, não apenas ausência de erro 500. Registrar resultado esperado/obtido, requisição sanitizada, impacto e correção ou limitação. Reutilizar evidências C02/S05 quando pertinente, acrescentando análise específica dos achados. Executar somente contra o ambiente de teste.

## 12. Checklist final da rubrica

- [ ] PDF da Etapa 1 conferido e entregue.
- [ ] Plano completo e atualizado com o que efetivamente foi executado.
- [ ] Análise estática integrada ao build, relatório e decisões sobre achados.
- [ ] Unitários relevantes com mocks criteriosos e cobertura mensurada.
- [ ] Componentes isolados com sucesso, erro e borda no build.
- [ ] Sistema com fluxos principais/alternativos, relatório e vídeos selecionados.
- [ ] Índice de evidências acessível e comandos reproduzidos em checkout limpo.
- [ ] Extras, se realizados, com cenários, critérios, resultados e análise.
