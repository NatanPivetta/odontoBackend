# API de Atividades — Design Spec
**Data:** 2026-05-04  
**Work items:** #23 (PBI), #24 (feedback privado), #25 (multi-sessão), #39 (API base), #41 (campo feedback), #43 (modelo hierárquico)  
**Abordagem escolhida:** Módulo único `atividade`, feedback como campo na entidade

---

## Contexto

Alunos de odontologia registram atendimentos realizados em pacientes. O registro é baseado em uma ficha de papel com campos: data, paciente, observações, professor orientador, aluno e professor tutor. Um procedimento pode demandar múltiplas sessões — cada sessão adicional é uma nova `Atividade` vinculada à atividade inicial (pai). Professores podem adicionar um feedback privado por atividade.

---

## Entidade e Schema

### Enum `StatusAtividade`
```
PENDENTE | EM_ANDAMENTO | CONCLUIDA
```

### Entidade `Atividade` — tabela `atividades`
Herda `Auditable<String>` (createdAt, updatedAt, createdBy, lastModifiedBy).

| Coluna | Tipo SQL | Restrição |
|---|---|---|
| `id` | BIGINT | PK, GENERATED ALWAYS AS IDENTITY |
| `data` | DATE | NOT NULL |
| `data_conclusao` | DATE | NULL |
| `prontuario` | VARCHAR(20) | NOT NULL |
| `nome_paciente` | VARCHAR(150) | NULL |
| `observacoes` | TEXT | NULL |
| `feedback_privado` | TEXT | NULL |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDENTE' |
| `aluno_id` | BIGINT | NOT NULL, FK → users(id) |
| `professor_orientador_id` | BIGINT | NOT NULL, FK → users(id) |
| `professor_tutor_id` | BIGINT | NULL, FK → users(id) |
| `turma_id` | BIGINT | NOT NULL, FK → turmas(id) |
| `atividade_pai_id` | BIGINT | NULL, FK → atividades(id) |
| audit fields | — | createdAt, updatedAt, createdBy, lastModifiedBy |

Migration: `db.changelog-1.1.0.sql`

---

## Estrutura do Módulo

```
br.ufrgs.odonto.modules.atividade/
├── entity/
│   ├── Atividade.java
│   └── StatusAtividade.java (enum)
├── repository/
│   └── AtividadeRepository.java
├── service/
│   └── AtividadeService.java
├── mapper/
│   └── AtividadeMapper.java
└── resource/
    └── AtividadeResource.java

br.ufrgs.odonto.dto.atividade/
├── CreateAtividadeRequestDTO.java
├── UpdateAtividadeRequestDTO.java
└── AtividadeResponseDTO.java
```

---

## DTOs

### `CreateAtividadeRequestDTO` (record)
```
LocalDate data                    — obrigatório
LocalDate dataConclusao           — opcional (aluno preenche manualmente)
String prontuario                 — obrigatório, max 20
String nomePaciente               — opcional, max 150
String observacoes                — opcional
StatusAtividade status            — obrigatório
Long alunoId                      — obrigatório
Long professorOrientadorId        — obrigatório
Long professorTutorId             — opcional
Long turmaId                      — obrigatório
Long atividadePaiId               — opcional
```

### `UpdateAtividadeRequestDTO` (record)
Todos os campos opcionais (nullable). `feedbackPrivado` presente, mas só aplicado pelo service se caller for PROFESSOR.

### `AtividadeResponseDTO` (record)
Todos os campos da entidade. `feedbackPrivado` retornado somente se:
- caller tem role `PROFESSOR`, ou
- caller tem role `ALUNO` e `atividade.aluno.id == caller.id`

---

## Regras de Negócio

### Criação
- Aluno logado: `alunoId` deve ser igual ao próprio `id` (não pode registrar em nome de outro aluno)
- Professor logado: pode informar qualquer `alunoId` válido

### Leitura
- Professor: acessa qualquer atividade
- Aluno: acessa apenas atividades onde `aluno_id == caller.id`

### Atualização
- Mesma regra de acesso da leitura
- `feedbackPrivado`: somente professor pode alterar

### `dataConclusao`
- Campo manual, sem automação — o aluno pode registrar retroativamente ao final do turno
- Não há vínculo obrigatório com `status == CONCLUIDA`

### Exclusão
- Somente PROFESSOR

---

## Endpoints

```
POST   /v1/atividades              → criar atividade         (ALUNO ou PROFESSOR)
GET    /v1/atividades              → listar (paginado)        (ALUNO ou PROFESSOR)
GET    /v1/atividades/{id}         → buscar por id            (ALUNO ou PROFESSOR)
PUT    /v1/atividades/{id}         → atualizar                (ALUNO ou PROFESSOR)
PATCH  /v1/atividades/{id}/status  → atualizar status         (ALUNO ou PROFESSOR)
DELETE /v1/atividades/{id}         → excluir                  (apenas PROFESSOR)
```

---

## Segurança

- Todos os endpoints requerem autenticação JWT
- Controle de acesso por role e ownership implementado no `AtividadeService`
- `feedbackPrivado` nunca exposto via serialização direta — mapeado condicionalmente no mapper com base no usuário logado

---

## Fora do Escopo (desta sprint)

- Módulo de agendamento / delegação por monitores
- Paciente como entidade própria com cadastro
- Histórico de feedbacks (múltiplos por atividade)
- Filtros avançados (work item #35 — task separada)
- Endpoints batch (work item #37 — task separada)
