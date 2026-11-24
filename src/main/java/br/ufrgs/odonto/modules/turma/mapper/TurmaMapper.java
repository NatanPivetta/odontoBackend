package br.ufrgs.odonto.modules.turma.mapper;

import br.ufrgs.odonto.dto.turma.TurmaResumoDTO;
import br.ufrgs.odonto.dto.turma.TurmaResponseDTO;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = UserMapper.class)
public interface TurmaMapper {

    @Mapping(target = "alunos", source = "alunosAtivos")
    @Mapping(target = "disciplinaCodigo", expression = "java(codigoDisciplina(turma))")
    @Mapping(target = "disciplinaNome", expression = "java(nomeDisciplina(turma))")
    @Mapping(target = "disciplinaLabel", expression = "java(labelDisciplina(turma))")
    TurmaResponseDTO toResponse(Turma turma);

    @Mapping(target = "disciplinaCodigo", expression = "java(codigoDisciplina(turma))")
    @Mapping(target = "disciplinaNome", expression = "java(nomeDisciplina(turma))")
    @Mapping(target = "disciplinaLabel", expression = "java(labelDisciplina(turma))")
    TurmaResumoDTO toResumo(Turma turma);

    default String codigoDisciplina(Turma turma) {
        return turma.getDisciplina() != null ? turma.getDisciplina().getCodigo() : null;
    }

    default String nomeDisciplina(Turma turma) {
        return turma.getDisciplina() != null ? turma.getDisciplina().getNome() : null;
    }

    default String labelDisciplina(Turma turma) {
        return turma.getDisciplina() != null ? turma.getDisciplina().getLabel() : null;
    }
}
