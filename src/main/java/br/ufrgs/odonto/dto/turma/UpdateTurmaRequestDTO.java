package br.ufrgs.odonto.dto.turma;

import jakarta.validation.constraints.Size;

public record UpdateTurmaRequestDTO(

        @Size(max = 20)
        String disciplina,

        @Size(max = 100)
        String name,

        @Size(max = 10)
        String semester,

        Boolean active
) {}
