package br.ufrgs.odonto.modules.atividade.mapper;

import br.ufrgs.odonto.dto.atividade.FeedbackResponseDTO;
import br.ufrgs.odonto.modules.atividade.entity.Feedback;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {UserMapper.class})
public interface FeedbackMapper {
    FeedbackResponseDTO toResponse(Feedback feedback);
}