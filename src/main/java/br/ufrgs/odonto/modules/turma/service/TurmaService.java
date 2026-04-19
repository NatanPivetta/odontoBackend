package br.ufrgs.odonto.modules.turma.service;

import br.ufrgs.odonto.dto.turma.CreateTurmaRequestDTO;
import br.ufrgs.odonto.dto.turma.TurmaResponseDTO;
import br.ufrgs.odonto.dto.turma.UpdateTurmaRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.mapper.TurmaMapper;
import br.ufrgs.odonto.modules.turma.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
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
        User aluno = findAlunoById(alunoId);

        if (turma.getAlunos().contains(aluno)) {
            throw new BusinessException("Aluno já matriculado nesta turma");
        }

        turma.getAlunos().add(aluno);
        return turmaMapper.toResponse(turmaRepository.save(turma));
    }

    @Transactional
    public void removeAluno(Long turmaId, Long alunoId) {
        Turma turma = findTurmaById(turmaId);
        User aluno = findAlunoById(alunoId);

        if (!turma.getAlunos().remove(aluno)) {
            throw new BusinessException("Aluno não está matriculado nesta turma");
        }

        turmaRepository.save(turma);
    }

    private Turma findTurmaById(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + id));
    }

    private User findAlunoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (user.getRole() != Role.ALUNO) {
            throw new BusinessException("O usuário informado não possui o papel de ALUNO");
        }

        return user;
    }
}
