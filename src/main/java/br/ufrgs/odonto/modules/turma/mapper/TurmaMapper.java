package br.ufrgs.odonto.modules.turma.mapper;

import br.ufrgs.odonto.dto.turma.TurmaResumoDTO;
import br.ufrgs.odonto.dto.turma.TurmaResponseDTO;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = UserMapper.class)
public interface TurmaMapper {

    @Mapping(target = "alunos", source = "alunos")
    TurmaResponseDTO toResponse(Turma turma);

    TurmaResumoDTO toResumo(Turma turma);
}