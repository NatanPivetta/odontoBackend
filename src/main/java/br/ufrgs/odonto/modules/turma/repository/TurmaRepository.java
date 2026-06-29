package br.ufrgs.odonto.modules.turma.repository;

import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    @Query("""
            SELECT t FROM Turma t
            JOIN t.matriculas m
            WHERE t.active = true AND m.aluno = :aluno AND m.active = true
            ORDER BY t.id DESC
            LIMIT 1
            """)
    Optional<Turma> findActiveTurmaForAluno(@Param("aluno") User aluno);

    @Query("""
            SELECT t FROM Turma t
            JOIN t.matriculas m
            WHERE t.active = true AND m.aluno.id = :alunoId AND m.active = true
            """)
    List<Turma> findActiveTurmasForAlunoId(@Param("alunoId") Long alunoId);
}
