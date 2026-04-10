package br.ufrgs.odonto.dto.auth;

import br.ufrgs.odonto.enums.Role;

public record LoginResponseDTO(
        String token,
        String name,
        String cardNumber,
        Role role
) {}