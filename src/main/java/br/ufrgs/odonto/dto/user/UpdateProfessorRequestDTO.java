package br.ufrgs.odonto.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfessorRequestDTO(

        @Size(max = 150)
        String name,

        @Email
        @Size(max = 150)
        String email,

        Boolean active
) {}