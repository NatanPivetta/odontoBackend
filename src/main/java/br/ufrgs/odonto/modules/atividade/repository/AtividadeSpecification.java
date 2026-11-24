package br.ufrgs.odonto.modules.atividade.repository;

import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import br.ufrgs.odonto.modules.atividade.entity.TipoAtividade;
import org.springframework.data.jpa.domain.Specification;

public class AtividadeSpecification {

    public static Specification<Atividade> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Atividade> byAlunoId(Long alunoId) {
        return (root, query, cb) -> cb.equal(root.get("aluno").get("id"), alunoId);
    }

    public static Specification<Atividade> byTurmaId(Long turmaId) {
        return (root, query, cb) -> cb.equal(root.get("turma").get("id"), turmaId);
    }

    public static Specification<Atividade> byAtividadePaiId(Long atividadePaiId) {
        return (root, query, cb) -> cb.equal(root.get("atividadePai").get("id"), atividadePaiId);
    }

    public static Specification<Atividade> byProfessorOrientadorId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("professorOrientador").get("id"), id);
    }

    public static Specification<Atividade> byProfessorTutorId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("professorTutor").get("id"), id);
    }

    public static Specification<Atividade> byTipo(TipoAtividade tipo) {
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Atividade> byStatus(StatusAtividade status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
