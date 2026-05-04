package br.ufrgs.odonto.modules.atividade.repository;

import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    Page<Atividade> findByAlunoId(Long alunoId, Pageable pageable);

    Page<Atividade> findByTurmaId(Long turmaId, Pageable pageable);

    Page<Atividade> findByAlunoIdAndTurmaId(Long alunoId, Long turmaId, Pageable pageable);

    List<Atividade> findByAtividadePaiId(Long atividadePaiId);
}
