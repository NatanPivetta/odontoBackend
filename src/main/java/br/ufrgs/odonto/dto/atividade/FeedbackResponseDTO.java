package br.ufrgs.odonto.dto.atividade;

import br.ufrgs.odonto.dto.user.UserResponseDTO;

import java.time.LocalDateTime;

public record FeedbackResponseDTO(
        Long id,
        String texto,
        UserResponseDTO professor,
        LocalDateTime createdAt
) {}