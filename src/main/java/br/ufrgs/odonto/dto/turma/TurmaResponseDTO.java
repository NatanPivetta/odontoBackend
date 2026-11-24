package br.ufrgs.odonto.dto.turma;

import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.modules.turma.entity.DisciplinaClinica;

import java.time.LocalDateTime;
import java.util.List;

public record TurmaResponseDTO(
        Long id,
        DisciplinaClinica disciplina,
        String disciplinaCodigo,
        String disciplinaNome,
        String disciplinaLabel,
        String codigoTurma,
        String name,
        String semester,
        boolean active,
        List<UserResponseDTO> alunos,
        LocalDateTime createdAt
) {}
