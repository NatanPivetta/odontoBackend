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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AtividadeRepository atividadeRepository;
    private final FeedbackMapper feedbackMapper;

    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> findAll(Long atividadeId, User loggedUser) {
        Atividade atividade = findAtividadeById(atividadeId);
        if (loggedUser.getRole() == Role.ALUNO && !atividade.getAluno().getId().equals(loggedUser.getId())) {
            throw new ResourceNotFoundException(ErrorCode.ERROR_ACTIVITY_NOT_FOUND, atividadeId);
        }
        return feedbackRepository.findByAtividadeId(atividadeId).stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Transactional
    public FeedbackResponseDTO create(Long atividadeId, CreateFeedbackRequestDTO dto, User professor) {
        Atividade atividade = findAtividadeById(atividadeId);

        Feedback feedback = Feedback.builder()
                .texto(dto.texto())
                .atividade(atividade)
                .professor(professor)
                .build();

        return feedbackMapper.toResponse(feedbackRepository.save(feedback));
    }

    @Transactional
    public void delete(Long atividadeId, Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_FEEDBACK_NOT_FOUND, feedbackId));
        if (!feedback.getAtividade().getId().equals(atividadeId)) {
            throw new ResourceNotFoundException(ErrorCode.ERROR_FEEDBACK_NOT_FOUND, feedbackId);
        }
        feedbackRepository.delete(feedback);
    }

    private Atividade findAtividadeById(Long id) {
        return atividadeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_ACTIVITY_NOT_FOUND, id));
    }
}
