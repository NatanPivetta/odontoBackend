package br.ufrgs.odonto.modules.atividade.entity;

import br.ufrgs.odonto.config.audit.Auditable;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "atividades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Atividade extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(nullable = false, length = 20)
    private String prontuario;

    @Column(name = "nome_paciente", length = 150)
    private String nomePaciente;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "feedback_privado", columnDefinition = "TEXT")
    private String feedbackPrivado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusAtividade status = StatusAtividade.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private User aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_orientador_id", nullable = false)
    private User professorOrientador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_tutor_id")
    private User professorTutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atividade_pai_id")
    private Atividade atividadePai;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
