package br.ufrgs.odonto.modules.atividade.repository;

import br.ufrgs.odonto.modules.atividade.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByAtividadeId(Long atividadeId);
}