package br.ufrgs.odonto.modules.turma.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisciplinaClinica {
    CLINICA_ODONTOLOGICA_I("ODO99012", "CLINICA ODONTOLOGICA I"),
    CLINICA_ODONTOLOGICA_II("ODO99013", "CLINICA ODONTOLOGICA II"),
    CLINICA_ODONTOLOGICA_III("ODO99014", "CLINICA ODONTOLOGICA III"),
    CLINICA_ODONTOLOGICA_IV("ODO99016", "CLINICA ODONTOLOGICA IV");

    private final String codigo;
    private final String nome;

    public String getLabel() {
        return codigo + " - " + nome;
    }
}
