package br.ufrgs.odonto.dto.atividade;

import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusAtividadeRequestDTO(

        @NotNull
        StatusAtividade status
) {}
