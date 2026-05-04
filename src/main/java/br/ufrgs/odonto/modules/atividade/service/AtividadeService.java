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
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final UserRepository userRepository;
    private final TurmaRepository turmaRepository;
    private final AtividadeMapper atividadeMapper;

    @Transactional
    public AtividadeResponseDTO create(CreateAtividadeRequestDTO dto) {
        User aluno = findUserById(dto.alunoId());
        User professorOrientador = findUserById(dto.professorOrientadorId());
        User professorTutor = dto.professorTutorId() != null ? findUserById(dto.professorTutorId()) : null;
        Turma turma = findTurmaById(dto.turmaId());
        Atividade atividadePai = dto.atividadePaiId() != null ? findAtividadeById(dto.atividadePaiId()) : null;

        Atividade atividade = Atividade.builder()
                .data(dto.data())
                .dataConclusao(dto.dataConclusao())
                .prontuario(dto.prontuario())
                .nomePaciente(dto.nomePaciente())
                .observacoes(dto.observacoes())
                .status(dto.status() != null ? dto.status() : StatusAtividade.PENDENTE)
                .aluno(aluno)
                .professorOrientador(professorOrientador)
                .professorTutor(professorTutor)
                .turma(turma)
                .atividadePai(atividadePai)
                .build();

        return atividadeMapper.toResponse(atividadeRepository.save(atividade));
    }

    @Transactional
    public AtividadeResponseDTO createAsAluno(CreateAtividadeAlunoRequestDTO dto, User loggedUser) {
        User professorOrientador = findUserById(dto.professorOrientadorId());
        User professorTutor = dto.professorTutorId() != null ? findUserById(dto.professorTutorId()) : null;
        Turma turma = turmaRepository.findByActiveTrueAndAlunosContaining(loggedUser)
                .orElseThrow(() -> new BusinessException("Aluno não possui turma ativa no semestre atual"));
        Atividade atividadePai = dto.atividadePaiId() != null ? findAtividadeById(dto.atividadePaiId()) : null;

        Atividade atividade = Atividade.builder()
                .data(dto.data())
                .dataConclusao(dto.dataConclusao())
                .prontuario(dto.prontuario())
                .nomePaciente(dto.nomePaciente())
                .observacoes(dto.observacoes())
                .status(dto.status() != null ? dto.status() : StatusAtividade.PENDENTE)
                .aluno(loggedUser)
                .professorOrientador(professorOrientador)
                .professorTutor(professorTutor)
                .turma(turma)
                .atividadePai(atividadePai)
                .build();

        return atividadeMapper.toResponseSemFeedback(atividadeRepository.save(atividade));
    }

    @Transactional(readOnly = true)
    public Page<AtividadeResponseDTO> findAll(Pageable pageable, Long alunoId, Long turmaId) {
        Page<Atividade> page;
        if (alunoId != null && turmaId != null) {
            page = atividadeRepository.findByAlunoIdAndTurmaId(alunoId, turmaId, pageable);
        } else if (alunoId != null) {
            page = atividadeRepository.findByAlunoId(alunoId, pageable);
        } else if (turmaId != null) {
            page = atividadeRepository.findByTurmaId(turmaId, pageable);
        } else {
            page = atividadeRepository.findAll(pageable);
        }
        return page.map(atividadeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AtividadeResponseDTO> findMinhas(Pageable pageable, User loggedUser) {
        return atividadeRepository.findByAlunoId(loggedUser.getId(), pageable)
                .map(atividadeMapper::toResponseSemFeedback);
    }

    @Transactional(readOnly = true)
    public AtividadeResponseDTO findById(Long id, User loggedUser) {
        Atividade atividade = findAtividadeById(id);
        if (loggedUser.getRole() == Role.ALUNO) {
            if (!atividade.getAluno().getId().equals(loggedUser.getId())) {
                throw new ResourceNotFoundException("Atividade não encontrada: " + id);
            }
            return atividadeMapper.toResponseSemFeedback(atividade);
        }
        return atividadeMapper.toResponse(atividade);
    }

    @Transactional
    public AtividadeResponseDTO update(Long id, UpdateAtividadeRequestDTO dto) {
        Atividade atividade = findAtividadeById(id);

        if (dto.data() != null)            atividade.setData(dto.data());
        if (dto.dataConclusao() != null)   atividade.setDataConclusao(dto.dataConclusao());
        if (dto.prontuario() != null)      atividade.setProntuario(dto.prontuario());
        if (dto.nomePaciente() != null)    atividade.setNomePaciente(dto.nomePaciente());
        if (dto.observacoes() != null)     atividade.setObservacoes(dto.observacoes());
        if (dto.feedbackPrivado() != null) atividade.setFeedbackPrivado(dto.feedbackPrivado());
        if (dto.professorOrientadorId() != null) {
            atividade.setProfessorOrientador(findUserById(dto.professorOrientadorId()));
        }
        if (dto.professorTutorId() != null) {
            atividade.setProfessorTutor(findUserById(dto.professorTutorId()));
        }

        return atividadeMapper.toResponse(atividadeRepository.save(atividade));
    }

    @Transactional
    public AtividadeResponseDTO updateDadosBasicos(Long id, UpdateAtividadeRequestDTO dto, User loggedUser) {
        Atividade atividade = findAtividadeById(id);

        if (!atividade.getAluno().getId().equals(loggedUser.getId())) {
            throw new ResourceNotFoundException("Atividade não encontrada: " + id);
        }

        if (dto.data() != null)         atividade.setData(dto.data());
        if (dto.prontuario() != null)   atividade.setProntuario(dto.prontuario());
        if (dto.nomePaciente() != null) atividade.setNomePaciente(dto.nomePaciente());
        if (dto.observacoes() != null)  atividade.setObservacoes(dto.observacoes());

        return atividadeMapper.toResponseSemFeedback(atividadeRepository.save(atividade));
    }

    @Transactional
    public AtividadeResponseDTO updateStatus(Long id, UpdateStatusAtividadeRequestDTO dto) {
        Atividade atividade = findAtividadeById(id);
        atividade.setStatus(dto.status());
        return atividadeMapper.toResponse(atividadeRepository.save(atividade));
    }

    @Transactional
    public void delete(Long id) {
        Atividade atividade = findAtividadeById(id);
        atividade.setActive(false);
        atividadeRepository.save(atividade);
    }

    private Atividade findAtividadeById(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada: " + id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private Turma findTurmaById(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + id));
    }
}
