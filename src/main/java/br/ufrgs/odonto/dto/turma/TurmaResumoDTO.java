package br.ufrgs.odonto.dto.turma;

import br.ufrgs.odonto.modules.turma.entity.DisciplinaClinica;

public record TurmaResumoDTO(
        Long id,
        DisciplinaClinica disciplina,
        String disciplinaCodigo,
        String disciplinaNome,
        String disciplinaLabel,
        String codigoTurma,
        String name,
        String semester
) {}
