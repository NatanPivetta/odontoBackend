package br.ufrgs.odonto.dto.user;

import jakarta.validation.constraints.*;

public record CreateProfessorRequestDTO(

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
        String password
) {}