package br.ufrgs.odonto.modules.atividade.repository;

import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    Optional<Atividade> findByIdAndActiveTrue(Long id);

    Page<Atividade> findByActiveTrue(Pageable pageable);

    Page<Atividade> findByActiveTrueAndAlunoId(Long alunoId, Pageable pageable);

    Page<Atividade> findByActiveTrueAndTurmaId(Long turmaId, Pageable pageable);

    Page<Atividade> findByActiveTrueAndAlunoIdAndTurmaId(Long alunoId, Long turmaId, Pageable pageable);

    List<Atividade> findByActiveTrueAndAtividadePaiId(Long atividadePaiId);
}
