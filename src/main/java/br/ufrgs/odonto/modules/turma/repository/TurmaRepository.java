package br.ufrgs.odonto.modules.turma.repository;

import br.ufrgs.odonto.modules.turma.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
}