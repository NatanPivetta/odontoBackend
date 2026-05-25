package br.ufrgs.odonto.modules.turma.repository;

import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.entity.TurmaAluno;
import br.ufrgs.odonto.modules.turma.entity.TurmaAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurmaAlunoRepository extends JpaRepository<TurmaAluno, TurmaAlunoId> {

    List<TurmaAluno> findByAlunoAndActiveTrue(User aluno);

    Optional<TurmaAluno> findByTurmaAndAluno(Turma turma, User aluno);
}
