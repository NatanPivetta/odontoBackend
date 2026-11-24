package br.ufrgs.odonto.dto.user;

import br.ufrgs.odonto.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequestDTO(

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
        String cardNumber,

        @NotBlank
        @Size(min = 8, message = "A senha deve ter no minimo 8 caracteres")
        String password,

        @NotNull
        Role role
) {}
