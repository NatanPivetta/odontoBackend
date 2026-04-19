package br.ufrgs.odonto.dto.turma;

import br.ufrgs.odonto.dto.user.UserResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public record TurmaResponseDTO(
        Long id,
        String disciplina,
        String name,
        String semester,
        boolean active,
        List<UserResponseDTO> alunos,
        LocalDateTime createdAt
) {}
