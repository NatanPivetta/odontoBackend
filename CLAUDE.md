# CLAUDE.md — Regras de execução

> **Estas regras valem para o agente principal E para qualquer sub-agente despachado via `Task`, incluindo os agentes do `superpowers` (`spec-reviewer`, `code-reviewer`, `test-driven-development`, `verification-before-completion`, `implement-task`, etc.). Repasse estas regras ao delegar.**

---

## 1. O que você FAZ

Apenas duas coisas:

1. **Escreve e edita arquivos** com as ferramentas de filesystem (`Write`, `Edit`, `Read`).
2. **Lê arquivos existentes** para entender contexto antes de escrever.

Nada mais.

## 2. O que você NÃO FAZ — sob nenhuma circunstância

### Nunca execute comandos de build, teste ou execução

Proibidos, sem exceção:

- `./mvnw compile`, `./mvnw test`, `./mvnw clean`, `./mvnw package`, `./mvnw install`, `./mvnw spring-boot:run`, `./mvnw verify`
- `mvn ...` em qualquer variação
- `mvnw.cmd ...` em qualquer variação
- `gradle`, `gradlew`, `npm`, `yarn`, `pnpm`, `node` (este projeto é Java)
- Qualquer comando que inicie um processo, servidor, ou container

**Não pergunte se deve rodar. A resposta é não. Eu compilo e testo.**

### Nunca execute Git

Proibidos:

- `git status`, `git add`, `git commit`, `git push`, `git pull`, `git diff`, `git log`, `git show`, `git stash`, `git branch`, `git checkout`, `git reset`
- Qualquer comando começando com `git `

**Eu faço o versionamento. Nada de commits automáticos.**

### Nunca use shell para operações de filesystem

Não use `Bash`/`PowerShell` para tarefas que a ferramenta de filesystem já faz:

- ❌ `Test-Path`, `New-Item -ItemType Directory`, `mkdir`, `ls`, `dir`, `cat`, `Get-Content`
- ✅ Use `Read` para ler, `Write` para escrever (cria diretórios pais automaticamente), `Glob`/`Grep` para buscar.

A única exceção é se eu **explicitamente** pedir para você executar algo.

## 3. Override do workflow do `superpowers`

O `superpowers` por padrão executa um ciclo TDD (write test → run test → commit → review) e roda `mvn compile` para validação. **Neste projeto esse ciclo está desligado.** Sobrescreva o seguinte:

- Skill `test-driven-development`: escreva os testes como arquivos, **não os execute**.
- Skill `verification-before-completion`: a verificação é **manual feita por mim**. Considere a tarefa completa quando os arquivos estiverem escritos.
- Skill `code-reviewer` / `spec-reviewer`: pode ler arquivos e produzir um relatório textual, mas **não** roda `mvn`/`git`.
- Qualquer skill que sugira "vamos compilar para confirmar": ignorar e seguir.

Se uma skill insistir em executar comando proibido, **pare e me avise** — não execute mesmo assim.

## 4. Como entregar uma tarefa

1. Escreva os arquivos.
2. No final, liste os caminhos relativos criados/modificados.
3. Resuma decisões de design não óbvias em 1–2 frases por decisão.
4. Se introduzir uma dependência nova ou exigir configuração externa (variável de ambiente, `application.yml`), sinalize de forma destacada.
5. **Não diga** "agora vou compilar para verificar" nem "vou commitar". Apenas pare.

## 5. Convenções do projeto

- Stack: Spring Boot + Maven, Java. Estrutura por módulos: `br.ufrgs.odonto.modules.<modulo>.{resource,service,repository,dto,entity,mapper,exception}`.
- Espelhe a estrutura de um módulo existente (ex.: `turma`) ao criar um módulo novo.
- Não introduza dependências novas no `pom.xml` sem me avisar antes — descreva a dependência e espere confirmação.
- Siga o padrão de injeção por construtor já existente. Não use `@Autowired` em campo.
- Resources REST sob `resource/v1`.

## 6. Quando parar e perguntar

- Decisões de arquitetura não triviais (introduzir cache, fila, mudar padrão transacional).
- Ambiguidade sobre qual camada recebe a lógica.
- Mudança em contrato público (assinatura de endpoint, schema de banco, DTO já consumido).
- Skill do superpowers tentando forçar comando proibido (item 3).

Caso contrário: escreva e siga.