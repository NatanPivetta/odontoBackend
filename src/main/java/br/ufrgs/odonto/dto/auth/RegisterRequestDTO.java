package br.ufrgs.odonto.dto.auth;

import jakarta.validation.constraints.*;

public record RegisterRequestDTO(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(min = 9, max = 9, message = "O número do cartão deve ter exatamente 9 caracteres")
        String cardNumber,

        @NotBlank
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password,

        @NotBlank
        @Size(min = 6, max = 6, message = "O código deve ter exatamente 6 dígitos")
        String code
) {}
