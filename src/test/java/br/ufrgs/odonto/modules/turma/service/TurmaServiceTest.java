package br.ufrgs.odonto.modules.turma.service;

import br.ufrgs.odonto.dto.turma.BulkAlunoRequestDTO;
import br.ufrgs.odonto.dto.turma.UpdateTurmaRequestDTO;
import br.ufrgs.odonto.modules.turma.entity.DisciplinaClinica;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.entity.TurmaAluno;
import br.ufrgs.odonto.modules.turma.entity.TurmaAlunoId;
import br.ufrgs.odonto.modules.turma.mapper.TurmaMapper;
import br.ufrgs.odonto.modules.turma.repository.TurmaAlunoRepository;
import br.ufrgs.odonto.modules.turma.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TurmaServiceTest {

    private final TurmaRepository turmas = mock(TurmaRepository.class);
    private final TurmaAlunoRepository matriculas = mock(TurmaAlunoRepository.class);
    private final UserRepository usuarios = mock(UserRepository.class);
    private final TurmaMapper mapper = mock(TurmaMapper.class);
    private final TurmaService service = new TurmaService(turmas, matriculas, usuarios, mapper);

    private final Turma turma = Turma.builder().id(1L).build();
    private final User aluno = User.builder().id(10L).role(Role.ALUNO).build();

    @BeforeEach
    void preparaTurma() {
        when(turmas.findById(turma.getId())).thenReturn(Optional.of(turma));
    }

    @Test
    void criaMatriculaAtivaComChaveDaTurmaEDoAluno() {
        when(usuarios.findById(aluno.getId())).thenReturn(Optional.of(aluno));

        service.addAluno(turma.getId(), aluno.getId());

        var captor = ArgumentCaptor.forClass(TurmaAluno.class);
        verify(matriculas).save(captor.capture());
        var matricula = captor.getValue();
        assertThat(matricula.getId()).isEqualTo(new TurmaAlunoId(turma.getId(), aluno.getId()));
        assertThat(matricula.getTurma()).isSameAs(turma);
        assertThat(matricula.getAluno()).isSameAs(aluno);
        assertThat(matricula.isActive()).isTrue();
    }

    @Test
    void transferenciaDesativaMatriculaAnteriorEReativaVinculoExistente() {
        var anterior = TurmaAluno.builder().turma(Turma.builder().id(2L).build()).aluno(aluno).build();
        var destino = TurmaAluno.builder().turma(turma).aluno(aluno).active(false).build();
        when(usuarios.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(matriculas.findByTurmaAndAluno(turma, aluno)).thenReturn(Optional.of(destino));
        when(matriculas.findByAlunoAndActiveTrue(aluno)).thenReturn(List.of(anterior));

        service.addAluno(turma.getId(), aluno.getId());

        assertThat(anterior.isActive()).isFalse();
        assertThat(destino.isActive()).isTrue();
        var ordem = inOrder(matriculas);
        ordem.verify(matriculas).saveAll(List.of(anterior));
        ordem.verify(matriculas).save(destino);
    }

    @Test
    void rejeitaMatriculaDuplicadaSemAlterarVinculos() {
        when(usuarios.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(matriculas.findByTurmaAndAluno(turma, aluno))
                .thenReturn(Optional.of(TurmaAluno.builder().turma(turma).aluno(aluno).build()));

        assertThatThrownBy(() -> service.addAluno(turma.getId(), aluno.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_STUDENT_ALREADY_ENROLLED));
        verify(matriculas).findByTurmaAndAluno(turma, aluno);
        verifyNoMoreInteractions(matriculas);
    }

    @Test
    void rejeitaMatriculaEmTurmaFechada() {
        turma.setActive(false);

        assertThatThrownBy(() -> service.addAluno(turma.getId(), aluno.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_TURMA_CLOSED_ENROLLMENT));
        verifyNoInteractions(usuarios, matriculas);
    }

    @Test
    void rejeitaMatriculaDeProfessor() {
        var professor = User.builder().id(20L).role(Role.PROFESSOR).build();
        when(usuarios.findById(professor.getId())).thenReturn(Optional.of(professor));

        assertThatThrownBy(() -> service.addAluno(turma.getId(), professor.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_USER_NOT_STUDENT));
        verifyNoInteractions(matriculas);
    }

    @Test
    void removeVinculoEncontrado() {
        var matricula = TurmaAluno.builder().turma(turma).aluno(aluno).build();
        when(usuarios.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(matriculas.findByTurmaAndAluno(turma, aluno)).thenReturn(Optional.of(matricula));

        service.removeAluno(turma.getId(), aluno.getId());

        verify(matriculas).delete(matricula);
    }

    @Test
    void rejeitaRemocaoDeAlunoSemVinculo() {
        when(usuarios.findById(aluno.getId())).thenReturn(Optional.of(aluno));

        assertThatThrownBy(() -> service.removeAluno(turma.getId(), aluno.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_STUDENT_NOT_ENROLLED));
        verify(matriculas, never()).delete(any(TurmaAluno.class));
    }

    @Test
    void matriculaEmLotePreservaVinculoAtivoECadastraAlunoNovo() {
        var outroAluno = User.builder().id(11L).role(Role.ALUNO).build();
        var existente = TurmaAluno.builder().turma(turma).aluno(aluno).build();
        var ids = List.of(aluno.getId(), outroAluno.getId());
        when(usuarios.findAllById(ids)).thenReturn(List.of(aluno, outroAluno));
        when(matriculas.findByTurmaAndAluno(turma, aluno)).thenReturn(Optional.of(existente));

        service.addAlunosBulk(turma.getId(), new BulkAlunoRequestDTO(ids));

        var captor = ArgumentCaptor.forClass(TurmaAluno.class);
        verify(matriculas).save(captor.capture());
        assertThat(captor.getValue().getAluno()).isSameAs(outroAluno);
        assertThat(captor.getValue().isActive()).isTrue();
        assertThat(existente.isActive()).isTrue();
        verify(matriculas, never()).findByAlunoAndActiveTrue(aluno);
    }

    @Test
    void atualizaTodosOsCamposInformados() {
        var request = new UpdateTurmaRequestDTO(DisciplinaClinica.ODO99012, "A", "Clínica A", "2026/2", false);

        service.update(turma.getId(), request);

        assertThat(turma.getDisciplina()).isEqualTo(DisciplinaClinica.ODO99012);
        assertThat(turma.getCodigoTurma()).isEqualTo("A");
        assertThat(turma.getName()).isEqualTo("Clínica A");
        assertThat(turma.getSemester()).isEqualTo("2026/2");
        assertThat(turma.isActive()).isFalse();
        verify(turmas).save(turma);
    }

    @Test
    void edicaoParcialPreservaOsCamposOmitidos() {
        turma.setDisciplina(DisciplinaClinica.ODO99012);
        turma.setCodigoTurma("A");
        turma.setName("Clínica A");
        turma.setSemester("2026/1");

        service.update(turma.getId(), new UpdateTurmaRequestDTO(null, null, null, "2026/2", null));

        assertThat(turma.getDisciplina()).isEqualTo(DisciplinaClinica.ODO99012);
        assertThat(turma.getCodigoTurma()).isEqualTo("A");
        assertThat(turma.getName()).isEqualTo("Clínica A");
        assertThat(turma.getSemester()).isEqualTo("2026/2");
        assertThat(turma.isActive()).isTrue();
        verify(turmas).save(turma);
    }

    @Test
    void matriculaEmLoteRejeitaProfessorSemGravarVinculo() {
        var professor = User.builder().id(20L).role(Role.PROFESSOR).build();
        var ids = List.of(professor.getId());
        when(usuarios.findAllById(ids)).thenReturn(List.of(professor));

        assertThatThrownBy(() -> service.addAlunosBulk(turma.getId(), new BulkAlunoRequestDTO(ids)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ERROR_USER_NOT_STUDENT));
        verifyNoInteractions(matriculas);
    }

    @Test
    void remocaoEmLoteRemoveSomenteVinculosExistentes() {
        var outroAluno = User.builder().id(11L).role(Role.ALUNO).build();
        var ids = List.of(aluno.getId(), outroAluno.getId());
        var existente = TurmaAluno.builder().turma(turma).aluno(aluno).build();
        when(usuarios.findAllById(ids)).thenReturn(List.of(aluno, outroAluno));
        when(matriculas.findByTurmaAndAluno(turma, aluno)).thenReturn(Optional.of(existente));

        service.removeAlunosBulk(turma.getId(), new BulkAlunoRequestDTO(ids));

        verify(matriculas).delete(existente);
        verify(matriculas, times(1)).delete(any(TurmaAluno.class));
    }
}
