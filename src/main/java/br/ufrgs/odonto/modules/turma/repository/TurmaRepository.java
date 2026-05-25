package br.ufrgs.odonto.modules.turma.repository;

import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    @Query("""
            SELECT t FROM Turma t
            JOIN t.matriculas m
            WHERE t.active = true AND m.aluno = :aluno AND m.active = true
            """)
    Optional<Turma> findActiveTurmaForAluno(@Param("aluno") User aluno);
}
