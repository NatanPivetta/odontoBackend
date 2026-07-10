package br.ufrgs.odonto.dto.turma;

import br.ufrgs.odonto.modules.turma.entity.DisciplinaClinica;

public record DisciplinaClinicaDTO(
        DisciplinaClinica value,
        String codigo,
        String nome,
        String label
) {
    public static DisciplinaClinicaDTO from(DisciplinaClinica disciplina) {
        return new DisciplinaClinicaDTO(
                disciplina,
                disciplina.getCodigo(),
                disciplina.getNome(),
                disciplina.getLabel()
        );
    }
}
