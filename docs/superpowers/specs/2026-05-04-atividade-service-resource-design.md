# Design: AtividadeService e AtividadeResource

**Data:** 2026-05-04  
**Módulo:** `modules/atividade`  
**Status:** Aprovado

---

## Contexto

O módulo de Atividades já possui entity, DTOs, mapper e repository implementados. Este spec cobre a camada de service e resource (controller REST) seguindo o padrão do módulo `turma`.

---

## AtividadeService

**Localização:** `modules/atividade/service/AtividadeService.java`

**Dependências injetadas:**
- `AtividadeRepository`
- `UserRepository`
- `TurmaRepository`
- `AtividadeMapper`

### Métodos públicos (`@Transactional`)

| Método | Assinatura | Descrição |
|---|---|---|
| create (professor) | `create(CreateAtividadeRequestDTO dto, User loggedUser)` | Usa todos os campos do DTO. `alunoId`, `turmaId`, `professorOrientadorId` vêm do DTO. |
| create (aluno) | `createAsAluno(CreateAtividadeRequestDTO dto, User loggedUser)` | `alunoId = loggedUser.getId()`. `turmaId` é inferida buscando a turma ativa do aluno no semestre atual. `professorOrientadorId` vem do DTO. |
| findAll | `findAll(Pageable pageable, Long alunoId, Long turmaId)` | PROFESSOR. Filtros opcionais: `alunoId`, `turmaId`. Retorna com `feedbackPrivado`. |
| findMinhas | `findMinhas(Pageable pageable, User loggedUser)` | ALUNO. Lista atividades do aluno logado. Retorna sem `feedbackPrivado`. |
| findById | `findById(Long id, User loggedUser)` | Qualquer role. PROFESSOR recebe resposta com `feedbackPrivado`; ALUNO sem. Aluno só acessa a própria atividade. |
| update | `update(Long id, UpdateAtividadeRequestDTO dto)` | PROFESSOR. Atualiza dados básicos + `feedbackPrivado`. |
| updateDadosBasicos | `updateDadosBasicos(Long id, UpdateAtividadeRequestDTO dto, User loggedUser)` | ALUNO. Atualiza apenas `data`, `prontuario`, `nomePaciente`, `observacoes`. Valida que o aluno logado é o dono da atividade. |
| updateStatus | `updateStatus(Long id, UpdateStatusAtividadeRequestDTO dto)` | PROFESSOR. Atualiza apenas `status`. |
| delete | `delete(Long id)` | PROFESSOR. Soft delete via `setActive(false)`. |

### Helpers privados

- `findAtividadeById(Long id)` — lança `ResourceNotFoundException` se não encontrado ou inativo.
- `findUserById(Long id)` — lança `ResourceNotFoundException` se não encontrado.
- `findTurmaAtivaDoAluno(User aluno)` — busca a turma ativa do aluno no semestre atual; lança `BusinessException` se não encontrada.

---

## AtividadeResource

**Localização:** `modules/atividade/resource/v1/AtividadeResource.java`  
**Base path:** `/v1/atividades`

### Endpoints

| Método | Path | `@PreAuthorize` | Status | Descrição |
|---|---|---|---|---|
| `POST` | `/v1/atividades` | `hasRole('PROFESSOR')` | 201 | Professor cria atividade com todos os campos |
| `POST` | `/v1/atividades/aluno` | `hasRole('ALUNO')` | 201 | Aluno cria atividade; turmaId inferida automaticamente |
| `GET` | `/v1/atividades` | `hasRole('PROFESSOR')` | 200 | Lista paginada com filtros opcionais (`alunoId`, `turmaId`) |
| `GET` | `/v1/atividades/minhas` | `hasRole('ALUNO')` | 200 | Lista paginada das atividades do aluno logado |
| `GET` | `/v1/atividades/{id}` | `hasAnyRole('PROFESSOR','ALUNO')` | 200 | Detalhe; ALUNO não vê `feedbackPrivado` |
| `PUT` | `/v1/atividades/{id}` | `hasRole('PROFESSOR')` | 200 | Professor atualiza dados + feedback |
| `PATCH` | `/v1/atividades/{id}` | `hasRole('ALUNO')` | 200 | Aluno atualiza dados básicos |
| `PATCH` | `/v1/atividades/{id}/status` | `hasRole('PROFESSOR')` | 200 | Professor atualiza status |
| `DELETE` | `/v1/atividades/{id}` | `hasRole('PROFESSOR')` | 204 | Soft delete |

### Paginação (GETs de lista)

Parâmetros padrão: `page=0`, `size=20`, `sort="data"`, `direction=DESC`.

### Usuário autenticado

`@AuthenticationPrincipal UserDetails` injetado nos endpoints:
- `POST /aluno` — para inferir `alunoId` e `turmaId`
- `GET /minhas` — para filtrar pelo aluno logado
- `GET /{id}` — para verificar ownership (ALUNO) e controlar visibilidade do feedback
- `PATCH /{id}` — para validar ownership antes de atualizar

### Documentação OpenAPI

Cada endpoint terá `@Operation(summary = "...")`, `@ApiResponse`, e `@SecurityRequirement(name = "bearerAuth")` seguindo o padrão do `TurmaResource`.

---

## Regras de negócio

1. **Ownership do ALUNO:** Aluno só pode visualizar, atualizar e listar as próprias atividades. Tentativa de acesso a atividade de outro aluno lança `ResourceNotFoundException` (sem revelar existência).
2. **Turma inferida:** Na criação pelo aluno, a turma é buscada pelo vínculo ativo do aluno no semestre atual. Se não houver turma ativa, lança `BusinessException`.
3. **feedbackPrivado:** Sempre omitido nas respostas para ALUNO (via `toResponseSemFeedback` do mapper).
4. **Soft delete:** `delete` define `active = false`; `findAtividadeById` filtra por `active = true`.
5. **Tipos de atividade (futuro):** Campo `tipo` será adicionado via enum em sprint futura.

---

## Padrões seguidos

- Segue estrutura de `TurmaService` / `TurmaResource`.
- Exceções: `ResourceNotFoundException` e `BusinessException` (já existentes).
- Mapper: `AtividadeMapper.toResponse()` para PROFESSOR, `toResponseSemFeedback()` para ALUNO.
- Nenhuma lógica de role dentro do service — separação feita via endpoints distintos no resource.
