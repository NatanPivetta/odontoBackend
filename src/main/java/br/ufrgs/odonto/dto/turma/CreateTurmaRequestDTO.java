package br.ufrgs.odonto.dto.turma;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTurmaRequestDTO(

        @NotBlank
        @Size(max = 20)
        String disciplina,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 10)
        String semester
) {}
