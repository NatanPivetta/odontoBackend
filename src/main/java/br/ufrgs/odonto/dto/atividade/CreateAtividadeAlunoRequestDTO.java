package br.ufrgs.odonto.dto.atividade;

import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAtividadeAlunoRequestDTO(

        @NotNull
        LocalDate data,

        LocalDate dataConclusao,

        @NotBlank
        @Size(max = 20)
        String prontuario,

        @Size(max = 150)
        String nomePaciente,

        String observacoes,

        StatusAtividade status,

        @NotNull
        Long professorOrientadorId,

        Long professorTutorId,

        Long atividadePaiId
) {}
