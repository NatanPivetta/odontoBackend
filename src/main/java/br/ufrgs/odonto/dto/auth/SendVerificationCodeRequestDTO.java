package br.ufrgs.odonto.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SendVerificationCodeRequestDTO(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(max = 8, message = "O numero do cartao deve ter no maximo 8 caracteres")
        @Pattern(regexp = "\\d+", message = "O numero do cartao deve conter apenas digitos")
        String cardNumber
) {}
