package br.ufrgs.odonto.dto.atividade;

import br.ufrgs.odonto.dto.turma.TurmaResumoDTO;
import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AtividadeResponseDTO(
        Long id,
        LocalDate data,
        LocalDate dataConclusao,
        String prontuario,
        String nomePaciente,
        String observacoes,
        String feedbackPrivado,
        StatusAtividade status,
        UserResponseDTO aluno,
        UserResponseDTO professorOrientador,
        UserResponseDTO professorTutor,
        TurmaResumoDTO turma,
        Long atividadePaiId,
        LocalDateTime createdAt
) {}
