package br.ufrgs.odonto.modules.atividade.service;

import br.ufrgs.odonto.dto.atividade.*;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
                null, null, null, StatusAtividade.PENDENTE,
                null, null, null, null, null, null);
    }

    @Test
    void create_deveSalvarAtividadeComTodosOsCampos() {
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, 1L, 2L, null, 10L, null);

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
    void create_deveLancarExcecaoQuandoAlunoNaoEncontrado() {
        CreateAtividadeRequestDTO dto = new CreateAtividadeRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, 99L, 2L, null, 10L, null);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAsAluno_deveInferirTurmaDoAluno() {
        CreateAtividadeAlunoRequestDTO dto = new CreateAtividadeAlunoRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                StatusAtividade.PENDENTE, 2L, null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findByActiveTrueAndAlunosContaining(aluno))
                .thenReturn(Optional.of(turma));
        when(atividadeRepository.save(any())).thenReturn(atividade);
        when(atividadeMapper.toResponseSemFeedback(atividade)).thenReturn(responseDTO);

        AtividadeResponseDTO result = atividadeService.createAsAluno(dto, aluno);

        assertThat(result).isNotNull();
        verify(turmaRepository).findByActiveTrueAndAlunosContaining(aluno);
        verify(atividadeRepository).save(argThat(a -> a.getTurma().equals(turma)));
    }

    @Test
    void createAsAluno_deveLancarExcecaoQuandoSemTurmaAtiva() {
        CreateAtividadeAlunoRequestDTO dto = new CreateAtividadeAlunoRequestDTO(
                LocalDate.now(), null, "ABC123", null, null,
                null, 2L, null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findByActiveTrueAndAlunosContaining(aluno))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.createAsAluno(dto, aluno))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("turma ativa");
    }

    @Test
    void findById_professorRecebeRespostaComFeedback() {
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeMapper.toResponse(atividade)).thenReturn(responseDTO);

        AtividadeResponseDTO result = atividadeService.findById(100L, professor);

        assertThat(result).isEqualTo(responseDTO);
        verify(atividadeMapper).toResponse(atividade);
        verify(atividadeMapper, never()).toResponseSemFeedback(any());
    }

    @Test
    void findById_alunoRecebeRespostaSemFeedback() {
        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeMapper.toResponseSemFeedback(atividade)).thenReturn(responseDTO);

        AtividadeResponseDTO result = atividadeService.findById(100L, aluno);

        assertThat(result).isEqualTo(responseDTO);
        verify(atividadeMapper).toResponseSemFeedback(atividade);
        verify(atividadeMapper, never()).toResponse(any());
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
                LocalDate.now().plusDays(1), null, "XYZ999", "Paciente", null);

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(atividade));
        when(atividadeRepository.save(atividade)).thenReturn(atividade);
        when(atividadeMapper.toResponseSemFeedback(atividade)).thenReturn(responseDTO);

        atividadeService.updateDadosBasicos(100L, dto, aluno);

        verify(atividadeRepository).save(argThat(a ->
                a.getProntuario().equals("XYZ999") && a.getNomePaciente().equals("Paciente")));
    }

    @Test
    void updateDadosBasicos_alunoNaoPodeEditarAtividadeDeOutroAluno() {
        User outroAluno = User.builder().id(99L).role(Role.ALUNO).build();
        UpdateAtividadeAlunoRequestDTO dto = new UpdateAtividadeAlunoRequestDTO(
                null, null, "XYZ", null, null);

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

        atividadeService.updateStatus(100L, dto);

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
        // After delete, findByIdAndActiveTrue should return empty
        Atividade inativa = Atividade.builder()
                .id(100L)
                .active(false)
                .aluno(aluno)
                .build();

        when(atividadeRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.findById(100L, professor))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
