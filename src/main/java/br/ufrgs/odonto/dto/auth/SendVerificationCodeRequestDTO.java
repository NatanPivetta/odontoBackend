package br.ufrgs.odonto.dto.auth;

import jakarta.validation.constraints.*;

public record SendVerificationCodeRequestDTO(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(min = 9, max = 9, message = "O número do cartão deve ter exatamente 9 caracteres")
        String cardNumber
) {}
