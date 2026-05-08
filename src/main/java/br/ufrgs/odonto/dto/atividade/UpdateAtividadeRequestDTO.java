package br.ufrgs.odonto.dto.atividade;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAtividadeRequestDTO(

        LocalDate data,

        LocalDate dataConclusao,

        @Size(max = 20)
        String prontuario,

        @Size(max = 150)
        String nomePaciente,

        String observacoes,

        Long professorOrientadorId,

        Long professorTutorId
) {}