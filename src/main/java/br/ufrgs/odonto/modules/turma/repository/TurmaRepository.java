package br.ufrgs.odonto.modules.turma.repository;

import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    Optional<Turma> findByActiveTrueAndAlunosContaining(User aluno);
}