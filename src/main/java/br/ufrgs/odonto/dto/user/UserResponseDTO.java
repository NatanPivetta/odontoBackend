package br.ufrgs.odonto.dto.user;

import br.ufrgs.odonto.enums.Role;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String cardNumber,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {}
