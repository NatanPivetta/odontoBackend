package br.ufrgs.odonto.dto.turma;

import br.ufrgs.odonto.dto.user.UserResponseDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TurmaResponseDTO(
        UUID id,
        String disciplina,
        String name,
        String semester,
        boolean active,
        List<UserResponseDTO> alunos,
        OffsetDateTime createdAt
) {}
