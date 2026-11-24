package br.ufrgs.odonto.modules.turma.entity;

import br.ufrgs.odonto.modules.core.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "turma_alunos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurmaAluno {

    @EmbeddedId
    private TurmaAlunoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("turmaId")
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("alunoId")
    @JoinColumn(name = "aluno_id")
    private User aluno;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
