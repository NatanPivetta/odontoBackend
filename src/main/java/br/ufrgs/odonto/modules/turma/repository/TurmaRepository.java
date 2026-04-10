package br.ufrgs.odonto.modules.turma.repository;

import br.ufrgs.odonto.modules.turma.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {
}