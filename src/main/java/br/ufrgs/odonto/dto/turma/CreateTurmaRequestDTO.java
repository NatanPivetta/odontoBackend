package br.ufrgs.odonto.dto.turma;

import br.ufrgs.odonto.modules.turma.entity.DisciplinaClinica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTurmaRequestDTO(

        @NotNull
        DisciplinaClinica disciplina,

        @NotBlank
        @Size(max = 30)
        String codigoTurma,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 10)
        String semester
) {}
