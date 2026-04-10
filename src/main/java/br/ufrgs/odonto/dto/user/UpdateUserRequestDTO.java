package br.ufrgs.odonto.dto.user;

import br.ufrgs.odonto.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(

        @Size(max = 150)
        String name,

        @Email
        @Size(max = 150)
        String email,

        Role role,

        Boolean active
) {}
