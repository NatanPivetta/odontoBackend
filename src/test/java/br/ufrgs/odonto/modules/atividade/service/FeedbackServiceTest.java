package br.ufrgs.odonto.modules.atividade.service;

import br.ufrgs.odonto.dto.atividade.CreateFeedbackRequestDTO;
import br.ufrgs.odonto.dto.atividade.FeedbackResponseDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import br.ufrgs.odonto.modules.atividade.entity.Feedback;
import br.ufrgs.odonto.modules.atividade.mapper.FeedbackMapper;
import br.ufrgs.odonto.modules.atividade.repository.AtividadeRepository;
import br.ufrgs.odonto.modules.atividade.repository.FeedbackRepository;
import br.ufrgs.odonto.modules.core.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackServiceTest {

    private final FeedbackRepository feedbacks = mock(FeedbackRepository.class);
    private final AtividadeRepository atividades = mock(AtividadeRepository.class);
    private final FeedbackMapper mapper = mock(FeedbackMapper.class);
    private final FeedbackService service = new FeedbackService(feedbacks, atividades, mapper);
    private final User aluno = User.builder().id(10L).role(Role.ALUNO).build();
    private final User professor = User.builder().id(20L).role(Role.PROFESSOR).build();
    private final Atividade atividade = Atividade.builder().id(1L).aluno(aluno).build();

    @Test
    void criaFeedbackVinculadoAAtividadeEAoProfessor() {
        var dto = new CreateFeedbackRequestDTO("Revisar a descrição da atividade");
        var response = new FeedbackResponseDTO(2L, dto.texto(), null, null);
        when(atividades.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(atividade));
        when(feedbacks.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Feedback.class))).thenReturn(response);

        assertThat(service.create(1L, dto, professor)).isSameAs(response);

        var captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbacks).save(captor.capture());
        var feedback = captor.getValue();
        assertThat(feedback.getTexto()).isEqualTo(dto.texto());
        assertThat(feedback.getAtividade()).isSameAs(atividade);
        assertThat(feedback.getProfessor()).isSameAs(professor);
    }

    @Test
    void listaFeedbackParaAlunoDaAtividadeEProfessor() {
        var feedback = Feedback.builder().atividade(atividade).professor(professor).build();
        var response = new FeedbackResponseDTO(2L, "Revisar", null, null);
        when(atividades.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(atividade));
        when(feedbacks.findByAtividadeId(1L)).thenReturn(List.of(feedback));
        when(mapper.toResponse(feedback)).thenReturn(response);

        assertThat(service.findAll(1L, aluno)).containsExactly(response);
        assertThat(service.findAll(1L, professor)).containsExactly(response);
    }

    @Test
    void outroAlunoRecebeErroSemConsultarFeedbacks() {
        var outroAluno = User.builder().id(11L).role(Role.ALUNO).build();
        when(atividades.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(atividade));

        assertThatThrownBy(() -> service.findAll(1L, outroAluno))
                .isInstanceOfSatisfying(ResourceNotFoundException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_ACTIVITY_NOT_FOUND));
        verifyNoInteractions(feedbacks);
    }

    @Test
    void atividadeInexistenteImpedeCriacao() {
        assertThatThrownBy(() -> service.create(1L, new CreateFeedbackRequestDTO("Revisar"), professor))
                .isInstanceOfSatisfying(ResourceNotFoundException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_ACTIVITY_NOT_FOUND));
        verifyNoInteractions(feedbacks);
    }

    @Test
    void excluiFeedbackSomenteDaAtividadeInformada() {
        var feedback = Feedback.builder().atividade(atividade).build();
        when(feedbacks.findById(2L)).thenReturn(Optional.of(feedback));

        assertThatThrownBy(() -> service.delete(3L, 2L))
                .isInstanceOfSatisfying(ResourceNotFoundException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_FEEDBACK_NOT_FOUND));
        verify(feedbacks, never()).delete(any(Feedback.class));

        service.delete(1L, 2L);

        verify(feedbacks).delete(feedback);
    }

    @Test
    void feedbackInexistenteImpedeExclusao() {
        assertThatThrownBy(() -> service.delete(1L, 2L))
                .isInstanceOfSatisfying(ResourceNotFoundException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_FEEDBACK_NOT_FOUND));
        verify(feedbacks, never()).delete(any(Feedback.class));
    }
}
