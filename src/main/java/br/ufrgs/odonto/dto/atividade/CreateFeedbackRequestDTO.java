package br.ufrgs.odonto.dto.atividade;

import jakarta.validation.constraints.NotBlank;

public record CreateFeedbackRequestDTO(
        @NotBlank
        String texto
) {}