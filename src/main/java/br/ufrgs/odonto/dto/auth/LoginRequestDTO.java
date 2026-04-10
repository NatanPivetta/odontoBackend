package br.ufrgs.odonto.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(

        @NotBlank(message = "Número do cartão é obrigatório")
        @Size(min = 9, max = 9, message = "Número do cartão deve ter 9 dígitos")
        String cardNumber,

        @NotBlank(message = "Senha é obrigatória")
        String password
) {}