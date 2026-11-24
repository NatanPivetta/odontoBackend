package br.ufrgs.odonto.dto.turma;

import br.ufrgs.odonto.modules.turma.entity.DisciplinaClinica;
import jakarta.validation.constraints.Size;

public record UpdateTurmaRequestDTO(

        DisciplinaClinica disciplina,

        @Size(max = 30)
        String codigoTurma,

        @Size(max = 100)
        String name,

        @Size(max = 10)
        String semester,

        Boolean active
) {}
