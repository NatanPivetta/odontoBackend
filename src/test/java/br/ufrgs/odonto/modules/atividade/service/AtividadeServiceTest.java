package br.ufrgs.odonto.modules.atividade.service;

import br.ufrgs.odonto.dto.atividade.*;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import br.ufrgs.odonto.modules.atividade.entity.TipoAtividade;
import br.ufrgs.odonto.modules.atividade.mapper.AtividadeMapper;
import br.ufrgs.odonto.modules.atividade.repository.AtividadeRepository;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.repository.TurmaRepository;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtividadeServiceTest {

    @Mock AtividadeRepository atividadeRepository;
    @Mock UserRepository userRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock AtividadeMapper atividadeMapper;

    @InjectMocks AtividadeService atividadeService;

    User aluno;
    User professor;
    Turma turma;
    Atividade atividade;
    AtividadeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        aluno = User.builder().id(1L).name("Aluno").role(Role.ALUNO).active(true).build();
        professor = User.builder().id(2L).name("Professor").role(Role.PROFESSOR).active(true).build();
        turma = Turma.builder().id(10L).name("Turma A").active(true).build();
        atividade = Atividade.builder()
                .id(100L)
                .data(LocalDate.now())
                .prontuario("ABC123")
                .status(StatusAtividade.PENDENTE)
                .aluno(aluno)
                .professorOrientador(professor)
                .turma(turma)
                .active(true)
                .build();
        responseDTO = new AtividadeResponseDTO(100L, LocalDate.now(), null, "ABC123",
                null, null, StatusAtividade.PENDENTE,
                null, null, null, null, null, null, null, null);
    }

    @Test
    void create_deveSalvarAtividadeComTodosOsCampos() {
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, null, null, 1L, 2L, null, 10L, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findById(10L)).thenReturn(Optional.of(turma));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        AtividadeResponseDTO result = atividadeService.create(dto);

        assertThat(result).isNotNull();
        verify(atividadeRepository).save(argThat(a ->
                a.getAluno().equals(aluno) &&
                a.getProfessorOrientador().equals(professor) &&
                a.getTurma().equals(turma) &&
                a.getProntuario().equals("ABC123")));
    }

    @Test
    void create_deveLancarExcecaoQuandoAlunoNaoTemRoleAluno() {
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, null, null, 2L, 2L, null, 10L, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));

        assertThatThrownBy(() -> atividadeService.create(dto))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_USER_NOT_STUDENT));
    }

    @Test
    void create_deveLancarExcecaoQuandoAlunoNaoEncontrado() {
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, null, null, 99L, 2L, null, 10L, null);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAsAluno_deveInferirTurmaDoAluno() {
        CreateAtividadeAlunoRequestDTO dto = new CreateAtividadeAlunoRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, null, null, 2L, null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findActiveTurmaForAluno(aluno))
                .thenReturn(Optional.of(turma));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        AtividadeResponseDTO result = atividadeService.createAsAluno(dto, aluno);

        assertThat(result).isNotNull();
        verify(turmaRepository).findActiveTurmaForAluno(aluno);
        verify(atividadeRepository).save(argThat(a -> a.getTurma().equals(turma)));
    }

    @Test
    void createAsAluno_deveLancarExcecaoQuandoSemTurmaAtiva() {
        CreateAtividadeAlunoRequestDTO dto = new CreateAtividadeAlunoRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                null, null, null, 2L, null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findActiveTurmaForAluno(aluno))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.createAsAluno(dto, aluno))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_STUDENT_WITHOUT_ACTIVE_TURMA));
    }

    @Test
    void findById_professorRecebeResposta() {
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        AtividadeResponseDTO result = atividadeService.findById(100L, professor);

        assertThat(result).isEqualTo(responseDTO);
        verify(atividadeMapper).toResponse(atividade);
    }

    @Test
    void findById_alunoRecebeResposta() {
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        AtividadeResponseDTO result = atividadeService.findById(100L, aluno);

        assertThat(result).isEqualTo(responseDTO);
        verify(atividadeMapper).toResponse(atividade);
    }

    @Test
    void findById_alunoNaoPodeVerAtividadeDeOutroAluno() {
        User outroAluno = User.builder().id(99L).role(Role.ALUNO).build();
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));

        assertThatThrownBy(() -> atividadeService.findById(100L, outroAluno))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDadosBasicos_alunoAtualizaCamposPermitidos() {
        UpdateAtividadeAlunoRequestDTO dto = new UpdateAtividadeAlunoRequestDTO(
                LocalDate.now().plusDays(1), null, "XYZ999", "Paciente", null, null, null, null);

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.save(atividade)).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        atividadeService.updateDadosBasicos(100L, dto, aluno);

        verify(atividadeRepository).save(argThat(a ->
                a.getProntuario().equals("XYZ999") && a.getNomePaciente().equals("Paciente")));
    }

    @Test
    void updateDadosBasicos_alunoNaoPodeEditarAtividadeDeOutroAluno() {
        User outroAluno = User.builder().id(99L).role(Role.ALUNO).build();
        UpdateAtividadeAlunoRequestDTO dto = new UpdateAtividadeAlunoRequestDTO(
                null, null, "XYZ", null, null, null, null, null);

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));

        assertThatThrownBy(() -> atividadeService.updateDadosBasicos(100L, dto, outroAluno))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_professorAtualizaStatus() {
        UpdateStatusAtividadeRequestDTO dto = new UpdateStatusAtividadeRequestDTO(StatusAtividade.CONCLUIDA);

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.save(atividade)).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        atividadeService.updateStatus(100L, dto, professor);

        verify(atividadeRepository).save(argThat(a -> a.getStatus() == StatusAtividade.CONCLUIDA));
    }

    @Test
    void delete_defineCampoActiveComoFalse() {
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));

        atividadeService.delete(100L);

        verify(atividadeRepository).save(argThat(a -> !a.isActive()));
    }

    @Test
    void delete_naoPodeAcessarAtividadeDeletada() {
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.findById(100L, professor))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Regras de data ────────────────────────────────────────────

    @Test
    void create_dataFuturaForcaStatusPendente() {
        LocalDate futuro = LocalDate.now().plusDays(5);
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                futuro, null, "ABC123", null, null,
                StatusAtividade.CONCLUIDA, null, null, 1L, 2L, null, 10L, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findById(10L)).thenReturn(Optional.of(turma));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        atividadeService.create(dto);

        verify(atividadeRepository).save(argThat(a -> a.getStatus() == StatusAtividade.PENDENTE));
    }

    @Test
    void create_dataHojeMantémStatusInformado() {
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.CONCLUIDA, null, null, 1L, 2L, null, 10L, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findById(10L)).thenReturn(Optional.of(turma));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        atividadeService.create(dto);

        verify(atividadeRepository).save(argThat(a -> a.getStatus() == StatusAtividade.CONCLUIDA));
    }

    @Test
    void create_dataConclusaoFuturaLancaExcecao() {
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), LocalDate.now().plusDays(1), "ABC123", null, null,
                StatusAtividade.PENDENTE, null, null, 1L, 2L, null, 10L, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(aluno));

        assertThatThrownBy(() -> atividadeService.create(dto))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_ACTIVITY_COMPLETION_DATE_FUTURE));
    }

    @Test
    void createAsAluno_dataFuturaForcaStatusPendente() {
        LocalDate futuro = LocalDate.now().plusDays(3);
        CreateAtividadeAlunoRequestDTO dto = new CreateAtividadeAlunoRequestDTO(
                futuro, null, "ABC123", null, null,
                StatusAtividade.CONCLUIDA, null, null, 2L, null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findActiveTurmaForAluno(aluno)).thenReturn(Optional.of(turma));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        atividadeService.createAsAluno(dto, aluno);

        verify(atividadeRepository).save(argThat(a -> a.getStatus() == StatusAtividade.PENDENTE));
    }

    @Test
    void updateDadosBasicos_dataConclusaoFuturaLancaExcecao() {
        UpdateAtividadeAlunoRequestDTO dto = new UpdateAtividadeAlunoRequestDTO(
                null, LocalDate.now().plusDays(1), null, null, null, null, null, null);

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));

        assertThatThrownBy(() -> atividadeService.updateDadosBasicos(100L, dto, aluno))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_ACTIVITY_COMPLETION_DATE_FUTURE));
    }

    @Test
    void update_dataConclusaoFuturaLancaExcecao() {
        UpdateAtividadeRequestDTO dto = new UpdateAtividadeRequestDTO(
                null, LocalDate.now().plusDays(1), null, null, null, null, null, null, null);

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));

        assertThatThrownBy(() -> atividadeService.update(100L, dto))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_ACTIVITY_COMPLETION_DATE_FUTURE));
    }

    @Test
    void findAll_semFiltros_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 20);
        when(atividadeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(atividade)));
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        Page<AtividadeResponseDTO> result = atividadeService.findAll(pageable, null, null, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(atividadeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_comAlunoId_retornaAtividadesDoAluno() {
        Pageable pageable = PageRequest.of(0, 20);
        when(atividadeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(atividade)));
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        Page<AtividadeResponseDTO> result = atividadeService.findAll(pageable, 1L, null, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(atividadeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_comTurmaId_retornaAtividadesDaTurma() {
        Pageable pageable = PageRequest.of(0, 20);
        when(atividadeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(atividade)));
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        Page<AtividadeResponseDTO> result = atividadeService.findAll(pageable, null, 10L, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(atividadeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_comAlunoEturma_retornaAtividadesFiltradas() {
        Pageable pageable = PageRequest.of(0, 20);
        when(atividadeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(atividade)));
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        Page<AtividadeResponseDTO> result = atividadeService.findAll(pageable, 1L, 10L, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(atividadeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void update_professorAtualizaCampos() {
        UpdateAtividadeRequestDTO dto = new UpdateAtividadeRequestDTO(
                LocalDate.now().plusDays(1), null, "NEW123", null, null, null, null, null, null);

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.save(atividade)).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        atividadeService.update(100L, dto);

        verify(atividadeRepository).save(argThat(a -> a.getProntuario().equals("NEW123")));
    }

    @Test
    void update_atividadeNaoEncontradaLancaExcecao() {
        UpdateAtividadeRequestDTO dto = new UpdateAtividadeRequestDTO(null, null, null, null, null, null, null, null, null);
        when(atividadeRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.update(999L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createFilha_deveHerdarPacienteDaAtividadePai() {
        atividade.setNomePaciente("Paciente original");
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), null, "ABC123", "Paciente diferente", null,
                StatusAtividade.PENDENTE, null, null, 1L, 2L, null, 10L, 100L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findById(10L)).thenReturn(Optional.of(turma));
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        atividadeService.create(dto);
        verify(atividadeRepository).save(argThat(a ->
                "Paciente original".equals(a.getNomePaciente()) && a.getAtividadePai() == atividade));
    }

    @Test
    void createFilhaAsAluno_deveHerdarPacienteDaAtividadePai() {
        atividade.setNomePaciente("Paciente original");
        CreateAtividadeAlunoRequestDTO dto = new CreateAtividadeAlunoRequestDTO(
                LocalDate.now(), null, "ABC123", "Paciente diferente", null,
                StatusAtividade.PENDENTE, null, null, 2L, null, 100L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findActiveTurmaForAluno(aluno)).thenReturn(Optional.of(turma));
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        atividadeService.createAsAluno(dto, aluno);
        verify(atividadeRepository).save(argThat(a ->
                "Paciente original".equals(a.getNomePaciente()) && a.getAtividadePai() == atividade));
    }

    @Test
    void professorConcedeAltaAAtividadeConcluida() {
        atividade.setStatus(StatusAtividade.CONCLUIDA);
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.save(atividade)).thenReturn(atividade);
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        assertThat(atividadeService.updateStatus(100L,
                new UpdateStatusAtividadeRequestDTO(StatusAtividade.ALTA), professor)).isSameAs(responseDTO);

        assertThat(atividade.getStatus()).isEqualTo(StatusAtividade.ALTA);
        verify(atividadeRepository).save(atividade);
    }

    @Test
    void professorNaoConcedeAltaAAtividadePendente() {
        rejeitaMudancaDeStatus(StatusAtividade.PENDENTE, StatusAtividade.ALTA, professor,
                ErrorCode.ERROR_ACTIVITY_ALTA_REQUIRES_CONCLUIDA);
    }

    @Test
    void alunoNaoConcedeAltaMesmoNaPropriaAtividadeConcluida() {
        rejeitaMudancaDeStatus(StatusAtividade.CONCLUIDA, StatusAtividade.ALTA, aluno,
                ErrorCode.ERROR_ACTIVITY_ALTA_PROFESSOR_ONLY);
    }

    @Test
    void professorNaoPodeReverterUmaAlta() {
        rejeitaMudancaDeStatus(StatusAtividade.ALTA, StatusAtividade.CONCLUIDA, professor,
                ErrorCode.ERROR_ACTIVITY_ALTA_IMMUTABLE);
    }

    private void rejeitaMudancaDeStatus(StatusAtividade atual, StatusAtividade solicitado, User autor,
                                        ErrorCode erroEsperado) {
        atividade.setStatus(atual);
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));

        assertThatThrownBy(() -> atividadeService.updateStatus(100L,
                new UpdateStatusAtividadeRequestDTO(solicitado), autor))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(erroEsperado));
        assertThat(atividade.getStatus()).isEqualTo(atual);
        verify(atividadeRepository, never()).save(any(Atividade.class));
    }

    @Test
    void alunoNaoAlteraStatusDaAtividadeDeOutroAluno() {
        var outroAluno = User.builder().id(9L).role(Role.ALUNO).build();
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));

        assertThatThrownBy(() -> atividadeService.updateStatus(100L,
                new UpdateStatusAtividadeRequestDTO(StatusAtividade.CONCLUIDA), outroAluno))
                .isInstanceOfSatisfying(ResourceNotFoundException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ERROR_ACTIVITY_NOT_FOUND));
        assertThat(atividade.getStatus()).isEqualTo(StatusAtividade.PENDENTE);
        verify(atividadeRepository, never()).save(any(Atividade.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void outrosExigeDescricaoPreenchida(String descricao) {
        when(userRepository.findById(1L)).thenReturn(Optional.of(aluno));
        var request = new CreateAtividadeRequestDTO(LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, TipoAtividade.OUTROS, descricao, 1L, 2L, null, 10L, null);

        assertThatThrownBy(() -> atividadeService.create(request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ERROR_ACTIVITY_TYPE_DESCRIPTION_REQUIRED));
        verifyNoInteractions(atividadeRepository);
    }

    @Test
    void atividadeDeOntemComDataDeConclusaoFicaConcluidaEPreservaDescricao() {
        var ontem = LocalDate.now().minusDays(1);
        var request = new CreateAtividadeRequestDTO(ontem, ontem, "ABC123", null, null,
                StatusAtividade.PENDENTE, TipoAtividade.OUTROS, "Acompanhamento", 1L, 2L, 2L, 10L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findById(10L)).thenReturn(Optional.of(turma));

        atividadeService.create(request);

        var captor = ArgumentCaptor.forClass(Atividade.class);
        verify(atividadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusAtividade.CONCLUIDA);
        assertThat(captor.getValue().getDataConclusao()).isEqualTo(ontem);
        assertThat(captor.getValue().getTipoDescricao()).isEqualTo("Acompanhamento");
        assertThat(captor.getValue().getProfessorTutor()).isSameAs(professor);
    }

    @Test
    void edicaoDoAlunoAtualizaConclusaoObservacoesTipoETutor() {
        var ontem = LocalDate.now().minusDays(1);
        var request = new UpdateAtividadeAlunoRequestDTO(null, ontem, null, null, "Retorno",
                TipoAtividade.OUTROS, "Acompanhamento", 2L);
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));

        atividadeService.updateDadosBasicos(100L, request, aluno);

        assertThat(atividade.getDataConclusao()).isEqualTo(ontem);
        assertThat(atividade.getObservacoes()).isEqualTo("Retorno");
        assertThat(atividade.getTipo()).isEqualTo(TipoAtividade.OUTROS);
        assertThat(atividade.getTipoDescricao()).isEqualTo("Acompanhamento");
        assertThat(atividade.getProfessorTutor()).isSameAs(professor);
        assertThat(atividade.getProntuario()).isEqualTo("ABC123");
        verify(atividadeRepository).save(atividade);
    }
}
