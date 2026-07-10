package br.ufrgs.odonto.modules.turma.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisciplinaClinica {
    ODO99012("CLINICA_ODONTOLOGICA_I"),
    ODO99013("CLINICA_ODONTOLOGICA_II"),
    ODO99014("CLINICA_ODONTOLOGICA_III"),
    ODO99016("CLINICA_ODONTOLOGICA_IV");

    private final String nome;

    public String getCodigo() {
        return name();
    }

    public String getLabel() {
        return getCodigo() + " - " + nome;
    }
}
