package br.ufrgs.odonto.modules.atividade.mapper;

import br.ufrgs.odonto.dto.atividade.AtividadeResponseDTO;
import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.turma.mapper.TurmaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UserMapper.class, TurmaMapper.class})
public interface AtividadeMapper {

    @Mapping(target = "atividadePaiId", source = "atividadePai.id")
    AtividadeResponseDTO toResponse(Atividade atividade);
}