package br.ufrgs.odonto.dto.user;

import br.ufrgs.odonto.enums.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        String cardNumber,
        Role role,
        boolean active,
        OffsetDateTime createdAt
) {}
