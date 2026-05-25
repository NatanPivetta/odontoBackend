package br.ufrgs.odonto.modules.turma.entity;

import br.ufrgs.odonto.config.audit.Auditable;
import br.ufrgs.odonto.modules.core.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "turmas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turma extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String disciplina;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String semester;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "turma", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TurmaAluno> matriculas = new HashSet<>();

    public Set<User> getAlunosAtivos() {
        return matriculas.stream()
                .filter(TurmaAluno::isActive)
                .map(TurmaAluno::getAluno)
                .collect(Collectors.toSet());
    }
}
