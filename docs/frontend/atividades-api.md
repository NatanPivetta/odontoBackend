# API de Atividades — Guia de Integração Frontend

Base URL: `/v1/atividades`  
Autenticação: Bearer token em todos os endpoints (`Authorization: Bearer <token>`)

---

## Objetos de referência

### AtividadeResponseDTO
```json
{
  "id": 1,
  "data": "2025-03-10",
  "dataConclusao": "2025-03-15",
  "prontuario": "ABC123",
  "nomePaciente": "João Silva",
  "observacoes": "Observações livres",
  "feedbackPrivado": "Só visível para o professor",
  "status": "PENDENTE",
  "aluno": { "id": 1, "name": "Maria", "email": "maria@ufrgs.br", "cardNumber": "12345", "role": "ALUNO", "active": true, "createdAt": "2025-01-01T10:00:00" },
  "professorOrientador": { "id": 2, "name": "Prof. Carlos", "email": "carlos@ufrgs.br", "cardNumber": null, "role": "PROFESSOR", "active": true, "createdAt": "2024-06-01T08:00:00" },
  "professorTutor": null,
  "turma": { "id": 10, "disciplina": "Clínica Integrada", "name": "Turma A", "semester": "2025/1" },
  "atividadePaiId": null,
  "createdAt": "2025-03-10T14:30:00"
}
```

> **Atenção — campo `feedbackPrivado`:** quando o endpoint é chamado por um aluno, este campo vem sempre como `null`, independentemente de ter conteúdo no banco. O frontend não deve exibi-lo para usuários com role `ALUNO`.

### Status possíveis
| Valor | Descrição |
|-------|-----------|
| `PENDENTE` | Padrão ao criar |
| `EM_ANDAMENTO` | Em execução |
| `CONCLUIDA` | Finalizada |

### Paginação (resposta)
Todos os endpoints de listagem retornam um objeto `Page` do Spring:
```json
{
  "content": [ /* array de AtividadeResponseDTO */ ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

---

## Endpoints

### 1. Criar atividade — Professor

```
POST /v1/atividades
Role: PROFESSOR
```

**Request body:**
```json
{
  "data": "2025-03-10",
  "dataConclusao": "2025-03-15",
  "prontuario": "ABC123",
  "nomePaciente": "João Silva",
  "observacoes": "Texto livre",
  "status": "PENDENTE",
  "alunoId": 1,
  "professorOrientadorId": 2,
  "professorTutorId": null,
  "turmaId": 10,
  "atividadePaiId": null
}
```

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `data` | `date` | sim | |
| `dataConclusao` | `date` | não | |
| `prontuario` | `string` | sim | máx 20 chars |
| `nomePaciente` | `string` | não | máx 150 chars |
| `observacoes` | `string` | não | |
| `status` | `StatusAtividade` | sim | |
| `alunoId` | `long` | sim | deve ser usuário com role `ALUNO` |
| `professorOrientadorId` | `long` | sim | |
| `professorTutorId` | `long` | não | |
| `turmaId` | `long` | sim | |
| `atividadePaiId` | `long` | não | ID de atividade-pai (atividades vinculadas) |

**Resposta:** `201 Created` → `AtividadeResponseDTO`

---

### 2. Criar atividade — Aluno

```
POST /v1/atividades/aluno
Role: ALUNO
```

A turma é inferida automaticamente da matrícula ativa do aluno autenticado; o aluno não informa `turmaId`.

**Request body:**
```json
{
  "data": "2025-03-10",
  "dataConclusao": null,
  "prontuario": "XYZ999",
  "nomePaciente": "Ana Costa",
  "observacoes": null,
  "status": "PENDENTE",
  "professorOrientadorId": 2,
  "professorTutorId": null,
  "atividadePaiId": null
}
```

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `data` | `date` | sim | |
| `dataConclusao` | `date` | não | |
| `prontuario` | `string` | sim | máx 20 chars |
| `nomePaciente` | `string` | não | máx 150 chars |
| `observacoes` | `string` | não | |
| `status` | `StatusAtividade` | não | default `PENDENTE` quando omitido |
| `professorOrientadorId` | `long` | sim | |
| `professorTutorId` | `long` | não | |
| `atividadePaiId` | `long` | não | |

**Resposta:** `201 Created` → `AtividadeResponseDTO` (campo `feedbackPrivado` sempre `null`)

**Erro comum:** `400 Bad Request` se o aluno não tiver turma ativa no semestre atual.

---

### 3. Listar atividades — Professor

```
GET /v1/atividades
Role: PROFESSOR
```

**Query params:**
| Param | Tipo | Obrigatório | Default | Descrição |
|-------|------|-------------|---------|-----------|
| `alunoId` | `long` | não | — | Filtra por aluno |
| `turmaId` | `long` | não | — | Filtra por turma |
| `page` | `int` | não | `0` | Página (0-indexado) |
| `size` | `int` | não | `20` | Itens por página |
| `sort` | `string` | não | `data` | Campo de ordenação |
| `direction` | `ASC\|DESC` | não | `DESC` | Direção da ordenação |

Filtros são combináveis: `?alunoId=1&turmaId=10` retorna apenas atividades daquele aluno naquela turma.

**Resposta:** `200 OK` → `Page<AtividadeResponseDTO>`

---

### 4. Listar minhas atividades — Aluno

```
GET /v1/atividades/minhas
Role: ALUNO
```

Retorna apenas as atividades do aluno autenticado. O campo `feedbackPrivado` vem sempre `null`.

**Query params:** `page`, `size`, `sort`, `direction` (mesmos defaults do endpoint 3)

**Resposta:** `200 OK` → `Page<AtividadeResponseDTO>`

---

### 5. Buscar atividade por ID

```
GET /v1/atividades/{id}
Role: PROFESSOR | ALUNO
```

- **Professor:** recebe todos os campos, incluindo `feedbackPrivado`.
- **Aluno:** recebe o campo `feedbackPrivado` como `null`. Se o `id` pertencer a outro aluno, retorna `404`.

**Resposta:** `200 OK` → `AtividadeResponseDTO`

---

### 6. Atualizar atividade — Professor

```
PUT /v1/atividades/{id}
Role: PROFESSOR
```

Atualização parcial: apenas os campos enviados com valor não-nulo são alterados.

**Request body:**
```json
{
  "data": "2025-03-12",
  "dataConclusao": null,
  "prontuario": null,
  "nomePaciente": null,
  "observacoes": "Nova observação",
  "feedbackPrivado": "Precisa revisar o prontuário",
  "professorOrientadorId": null,
  "professorTutorId": null
}
```

| Campo | Tipo | Regras |
|-------|------|--------|
| `data` | `date` | |
| `dataConclusao` | `date` | |
| `prontuario` | `string` | máx 20 chars |
| `nomePaciente` | `string` | máx 150 chars |
| `observacoes` | `string` | |
| `feedbackPrivado` | `string` | visível só ao professor |
| `professorOrientadorId` | `long` | substitui o orientador |
| `professorTutorId` | `long` | substitui o tutor |

**Resposta:** `200 OK` → `AtividadeResponseDTO`

---

### 7. Atualizar dados básicos — Aluno

```
PATCH /v1/atividades/{id}
Role: ALUNO
```

O aluno só pode editar sua própria atividade. Campos de feedback e status são ignorados pela camada de negócio (o DTO não os expõe).

**Request body:**
```json
{
  "data": "2025-03-11",
  "dataConclusao": "2025-03-14",
  "prontuario": "XYZ999",
  "nomePaciente": "Ana Costa",
  "observacoes": "Atualização do paciente"
}
```

| Campo | Tipo | Regras |
|-------|------|--------|
| `data` | `date` | |
| `dataConclusao` | `date` | |
| `prontuario` | `string` | máx 20 chars |
| `nomePaciente` | `string` | máx 150 chars |
| `observacoes` | `string` | |

**Resposta:** `200 OK` → `AtividadeResponseDTO` (campo `feedbackPrivado` sempre `null`)

**Erro comum:** `404 Not Found` ao tentar editar atividade de outro aluno.

---

### 8. Atualizar status — Professor

```
PATCH /v1/atividades/{id}/status
Role: PROFESSOR
```

**Request body:**
```json
{
  "status": "CONCLUIDA"
}
```

| Campo | Tipo | Obrigatório |
|-------|------|-------------|
| `status` | `StatusAtividade` | sim |

**Resposta:** `200 OK` → `AtividadeResponseDTO`

---

### 9. Desativar atividade (soft delete) — Professor

```
DELETE /v1/atividades/{id}
Role: PROFESSOR
```

Marca a atividade como inativa (`active = false`). A atividade deixa de aparecer em qualquer listagem ou busca por ID.

**Resposta:** `204 No Content`

---

## Resumo de permissões

| Endpoint | PROFESSOR | ALUNO |
|----------|-----------|-------|
| `POST /` | sim | não |
| `POST /aluno` | não | sim |
| `GET /` | sim | não |
| `GET /minhas` | não | sim |
| `GET /{id}` | sim (com feedback) | sim (sem feedback, só própria) |
| `PUT /{id}` | sim | não |
| `PATCH /{id}` | não | sim (só própria) |
| `PATCH /{id}/status` | sim | não |
| `DELETE /{id}` | sim | não |

---

## Códigos de resposta comuns

| Código | Situação |
|--------|----------|
| `200` | Operação bem-sucedida |
| `201` | Recurso criado |
| `204` | Soft delete executado |
| `400` | Corpo inválido (validação Bean Validation) ou regra de negócio violada (ex.: aluno sem turma ativa) |
| `401` | Token ausente ou expirado |
| `403` | Role sem permissão para o endpoint |
| `404` | Recurso não encontrado ou inativo |
