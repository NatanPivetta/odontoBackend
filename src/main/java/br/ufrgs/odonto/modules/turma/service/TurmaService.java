package br.ufrgs.odonto.modules.turma.service;

import br.ufrgs.odonto.dto.turma.BulkAlunoRequestDTO;
import br.ufrgs.odonto.dto.turma.CreateTurmaRequestDTO;
import br.ufrgs.odonto.dto.turma.TurmaResponseDTO;
import br.ufrgs.odonto.dto.turma.UpdateTurmaRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.entity.TurmaAluno;
import br.ufrgs.odonto.modules.turma.entity.TurmaAlunoId;
import br.ufrgs.odonto.modules.turma.mapper.TurmaMapper;
import br.ufrgs.odonto.modules.turma.repository.TurmaAlunoRepository;
import br.ufrgs.odonto.modules.turma.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final TurmaAlunoRepository turmaAlunoRepository;
    private final UserRepository userRepository;
    private final TurmaMapper turmaMapper;

    @Transactional(readOnly = true)
    public Page<TurmaResponseDTO> findAll(Pageable pageable) {
        return turmaRepository.findAll(pageable).map(turmaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TurmaResponseDTO findById(Long id) {
        return turmaMapper.toResponse(findTurmaById(id));
    }

    @Transactional(readOnly = true)
    public List<TurmaResponseDTO> findTurmasDoAluno(Long alunoId) {
        return turmaRepository.findActiveTurmasForAlunoId(alunoId)
                .stream().map(turmaMapper::toResponse).toList();
    }

    @Transactional
    public TurmaResponseDTO create(CreateTurmaRequestDTO request) {
        Turma turma = Turma.builder()
                .disciplina(request.disciplina())
                .name(request.name())
                .semester(request.semester())
                .build();

        return turmaMapper.toResponse(turmaRepository.save(turma));
    }

    @Transactional
    public TurmaResponseDTO update(Long id, UpdateTurmaRequestDTO request) {
        Turma turma = findTurmaById(id);

        if (request.disciplina() != null) turma.setDisciplina(request.disciplina());
        if (request.name() != null)       turma.setName(request.name());
        if (request.semester() != null)   turma.setSemester(request.semester());
        if (request.active() != null)     turma.setActive(request.active());

        return turmaMapper.toResponse(turmaRepository.save(turma));
    }

    @Transactional
    public void delete(Long id) {
        Turma turma = findTurmaById(id);
        turma.setActive(false);
        turmaRepository.save(turma);
    }

    @Transactional
    public TurmaResponseDTO addAluno(Long turmaId, Long alunoId) {
        Turma turma = findTurmaById(turmaId);
        validarTurmaAberta(turma);
        User aluno = findAlunoById(alunoId);

        if (matriculaAtivaExiste(turma, aluno)) {
            throw new BusinessException(ErrorCode.ERROR_STUDENT_ALREADY_ENROLLED);
        }

        desativarMatriculasAtivas(aluno);
        matricularOuReativar(turma, aluno);

        return turmaMapper.toResponse(findTurmaById(turmaId));
    }

    @Transactional
    public void removeAluno(Long turmaId, Long alunoId) {
        Turma turma = findTurmaById(turmaId);
        User aluno = findAlunoById(alunoId);

        TurmaAluno matricula = turmaAlunoRepository.findByTurmaAndAluno(turma, aluno)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERROR_STUDENT_NOT_ENROLLED));

        turmaAlunoRepository.delete(matricula);
    }

    @Transactional
    public TurmaResponseDTO addAlunosBulk(Long turmaId, BulkAlunoRequestDTO request) {
        Turma turma = findTurmaById(turmaId);
        validarTurmaAberta(turma);
        List<User> alunos = userRepository.findAllById(request.alunoIds());
        alunos.forEach(aluno -> {
            if (aluno.getRole() != Role.ALUNO) {
                throw new BusinessException(ErrorCode.ERROR_USER_NOT_STUDENT);
            }
            if (matriculaAtivaExiste(turma, aluno)) {
                return; // idempotente: já matriculado e ativo nesta turma, ignora
            }
            desativarMatriculasAtivas(aluno);
            matricularOuReativar(turma, aluno);
        });
        return turmaMapper.toResponse(findTurmaById(turmaId));
    }

    @Transactional
    public void removeAlunosBulk(Long turmaId, BulkAlunoRequestDTO request) {
        Turma turma = findTurmaById(turmaId);
        List<User> alunos = userRepository.findAllById(request.alunoIds());
        alunos.forEach(aluno ->
                turmaAlunoRepository.findByTurmaAndAluno(turma, aluno)
                        .ifPresent(turmaAlunoRepository::delete)
        );
    }

    private void desativarMatriculasAtivas(User aluno) {
        List<TurmaAluno> ativas = turmaAlunoRepository.findByAlunoAndActiveTrue(aluno);
        ativas.forEach(m -> m.setActive(false));
        turmaAlunoRepository.saveAll(ativas);
    }

    private void validarTurmaAberta(Turma turma) {
        if (!turma.isActive()) {
            throw new BusinessException(ErrorCode.ERROR_TURMA_CLOSED_ENROLLMENT);
        }
    }

    private boolean matriculaAtivaExiste(Turma turma, User aluno) {
        return turmaAlunoRepository.findByTurmaAndAluno(turma, aluno)
                .filter(TurmaAluno::isActive)
                .isPresent();
    }

    private void matricularOuReativar(Turma turma, User aluno) {
        TurmaAluno matricula = turmaAlunoRepository.findByTurmaAndAluno(turma, aluno)
                .orElseGet(() -> TurmaAluno.builder()
                        .id(new TurmaAlunoId(turma.getId(), aluno.getId()))
                        .turma(turma)
                        .aluno(aluno)
                        .build());
        matricula.setActive(true);
        turmaAlunoRepository.save(matricula);
    }

    private Turma findTurmaById(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_TURMA_NOT_FOUND, id));
    }

    private User findAlunoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND, id));

        if (user.getRole() != Role.ALUNO) {
            throw new BusinessException(ErrorCode.ERROR_USER_NOT_STUDENT);
        }

        return user;
    }
}
