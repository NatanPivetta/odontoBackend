package br.ufrgs.odonto.dto.atividade;

import br.ufrgs.odonto.modules.atividade.entity.TipoAtividade;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAtividadeAlunoRequestDTO(

        LocalDate data,

        LocalDate dataConclusao,

        @Size(max = 20)
        String prontuario,

        @Size(max = 150)
        String nomePaciente,

        String observacoes,

        TipoAtividade tipo,

        @Size(max = 255)
        String tipoDescricao,

        Long professorTutorId
) {}
