package br.ufrgs.odonto.modules.turma.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisciplinaClinica {
    ODO99012("CLÍNICA ODONTOLÓGICA I"),
    ODO99013("CLÍNICA ODONTOLÓGICA II"),
    ODO99014("CLÍNICA ODONTOLÓGICA III"),
    ODO99016("CLÍNICA ODONTOLÓGICA IV");

    private final String nome;

    public String getCodigo() {
        return name();
    }

    public String getLabel() {
        return getCodigo() + " - " + nome;
    }
}
