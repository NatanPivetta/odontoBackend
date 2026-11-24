package br.ufrgs.odonto.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(

        @NotBlank(message = "Numero do cartao e obrigatorio")
        @Size(max = 8, message = "Numero do cartao deve ter no maximo 8 digitos")
        @Pattern(regexp = "\\d+", message = "Numero do cartao deve conter apenas digitos")
        String cardNumber,

        @NotBlank(message = "Senha e obrigatoria")
        String password
) {}
