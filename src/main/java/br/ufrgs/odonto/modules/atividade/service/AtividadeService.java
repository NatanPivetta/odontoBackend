package br.ufrgs.odonto.modules.atividade.service;

import br.ufrgs.odonto.dto.atividade.*;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import br.ufrgs.odonto.modules.atividade.entity.TipoAtividade;
import br.ufrgs.odonto.modules.atividade.mapper.AtividadeMapper;
import br.ufrgs.odonto.modules.atividade.repository.AtividadeRepository;
import br.ufrgs.odonto.modules.atividade.repository.AtividadeSpecification;
import org.springframework.data.jpa.domain.Specification;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.Objects;
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
        if (aluno.getRole() != Role.ALUNO) {
            throw new BusinessException("O usuário informado não possui o perfil de aluno");
        }
        validarTipoDescricao(dto.tipo(), dto.tipoDescricao());
        validarDataConclusao(dto.dataConclusao());
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
                .status(resolverStatus(dto.data(), dto.status()))
                .tipo(dto.tipo())
                .tipoDescricao(dto.tipo() == TipoAtividade.OUTROS ? dto.tipoDescricao() : null)
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
        validarTipoDescricao(dto.tipo(), dto.tipoDescricao());
        validarDataConclusao(dto.dataConclusao());
        User professorOrientador = findUserById(dto.professorOrientadorId());
        User professorTutor = dto.professorTutorId() != null ? findUserById(dto.professorTutorId()) : null;
        Turma turma = turmaRepository.findActiveTurmaForAluno(loggedUser)
                .orElseThrow(() -> new BusinessException("Aluno não possui turma ativa no semestre atual"));
        Atividade atividadePai = dto.atividadePaiId() != null ? findAtividadeById(dto.atividadePaiId()) : null;

        Atividade atividade = Atividade.builder()
                .data(dto.data())
                .dataConclusao(dto.dataConclusao())
                .prontuario(dto.prontuario())
                .nomePaciente(dto.nomePaciente())
                .observacoes(dto.observacoes())
                .status(resolverStatus(dto.data(), Objects.requireNonNullElse(dto.status(), StatusAtividade.PENDENTE)))
                .tipo(dto.tipo())
                .tipoDescricao(dto.tipo() == TipoAtividade.OUTROS ? dto.tipoDescricao() : null)
                .aluno(loggedUser)
                .professorOrientador(professorOrientador)
                .professorTutor(professorTutor)
                .turma(turma)
                .atividadePai(atividadePai)
                .build();

        return atividadeMapper.toResponse(atividadeRepository.save(atividade));
    }

    @Transactional(readOnly = true)
    public Page<AtividadeResponseDTO> findAll(Pageable pageable, Long alunoId, Long turmaId,
                                              Long atividadePaiId, Long professorOrientadorId,
                                              Long professorTutorId, TipoAtividade tipo,
                                              StatusAtividade status) {
        Specification<Atividade> spec = Specification.where(AtividadeSpecification.isActive());
        if (alunoId != null)                spec = spec.and(AtividadeSpecification.byAlunoId(alunoId));
        if (turmaId != null)                spec = spec.and(AtividadeSpecification.byTurmaId(turmaId));
        if (atividadePaiId != null)         spec = spec.and(AtividadeSpecification.byAtividadePaiId(atividadePaiId));
        if (professorOrientadorId != null)  spec = spec.and(AtividadeSpecification.byProfessorOrientadorId(professorOrientadorId));
        if (professorTutorId != null)       spec = spec.and(AtividadeSpecification.byProfessorTutorId(professorTutorId));
        if (tipo != null)                   spec = spec.and(AtividadeSpecification.byTipo(tipo));
        if (status != null)                 spec = spec.and(AtividadeSpecification.byStatus(status));
        return atividadeRepository.findAll(spec, pageable).map(atividadeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AtividadeResponseDTO> findMinhas(Pageable pageable, User loggedUser, TipoAtividade tipo) {
        Specification<Atividade> spec = Specification.where(AtividadeSpecification.isActive())
                .and(AtividadeSpecification.byAlunoId(loggedUser.getId()));
        if (tipo != null) spec = spec.and(AtividadeSpecification.byTipo(tipo));
        return atividadeRepository.findAll(spec, pageable).map(atividadeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AtividadeResponseDTO findById(Long id, User loggedUser) {
        Atividade atividade = findAtividadeById(id);
        if (loggedUser.getRole() == Role.ALUNO && !atividade.getAluno().getId().equals(loggedUser.getId())) {
            throw new ResourceNotFoundException("Atividade não encontrada: " + id);
        }
        return atividadeMapper.toResponse(atividade);
    }

    @Transactional
    public AtividadeResponseDTO update(Long id, UpdateAtividadeRequestDTO dto) {
        Atividade atividade = findAtividadeById(id);

        if (dto.dataConclusao() != null)   validarDataConclusao(dto.dataConclusao());
        if (dto.data() != null)            atividade.setData(dto.data());
        if (dto.dataConclusao() != null)   atividade.setDataConclusao(dto.dataConclusao());
        if (dto.prontuario() != null)      atividade.setProntuario(dto.prontuario());
        if (dto.nomePaciente() != null)    atividade.setNomePaciente(dto.nomePaciente());
        if (dto.observacoes() != null)     atividade.setObservacoes(dto.observacoes());
        if (dto.tipo() != null) {
            validarTipoDescricao(dto.tipo(), dto.tipoDescricao());
            atividade.setTipo(dto.tipo());
            atividade.setTipoDescricao(dto.tipo() == TipoAtividade.OUTROS ? dto.tipoDescricao() : null);
        }
        if (dto.professorOrientadorId() != null) {
            atividade.setProfessorOrientador(findUserById(dto.professorOrientadorId()));
        }
        if (dto.professorTutorId() != null) {
            atividade.setProfessorTutor(findUserById(dto.professorTutorId()));
        }

        return atividadeMapper.toResponse(atividadeRepository.save(atividade));
    }

    @Transactional
    public AtividadeResponseDTO updateDadosBasicos(Long id, UpdateAtividadeAlunoRequestDTO dto, User loggedUser) {
        Atividade atividade = findAtividadeById(id);

        if (!atividade.getAluno().getId().equals(loggedUser.getId())) {
            throw new ResourceNotFoundException("Atividade não encontrada: " + id);
        }

        if (dto.dataConclusao() != null)   validarDataConclusao(dto.dataConclusao());
        if (dto.data() != null)            atividade.setData(dto.data());
        if (dto.dataConclusao() != null)   atividade.setDataConclusao(dto.dataConclusao());
        if (dto.prontuario() != null)      atividade.setProntuario(dto.prontuario());
        if (dto.nomePaciente() != null)    atividade.setNomePaciente(dto.nomePaciente());
        if (dto.observacoes() != null)     atividade.setObservacoes(dto.observacoes());
        if (dto.tipo() != null) {
            validarTipoDescricao(dto.tipo(), dto.tipoDescricao());
            atividade.setTipo(dto.tipo());
            atividade.setTipoDescricao(dto.tipo() == TipoAtividade.OUTROS ? dto.tipoDescricao() : null);
        }
        if (dto.professorTutorId() != null) {
            atividade.setProfessorTutor(findUserById(dto.professorTutorId()));
        }

        return atividadeMapper.toResponse(atividadeRepository.save(atividade));
    }

    @Transactional
    public AtividadeResponseDTO updateStatus(Long id, UpdateStatusAtividadeRequestDTO dto, User loggedUser) {
        Atividade atividade = findAtividadeById(id);
        if (loggedUser.getRole() == Role.ALUNO && !atividade.getAluno().getId().equals(loggedUser.getId())) {
            throw new ResourceNotFoundException("Atividade não encontrada: " + id);
        }
        if (dto.status() == StatusAtividade.ALTA) {
            if (loggedUser.getRole() != Role.PROFESSOR) {
                throw new BusinessException("Apenas professores podem registrar a alta do paciente");
            }
            if (atividade.getStatus() != StatusAtividade.CONCLUIDA) {
                throw new BusinessException("A alta só pode ser registrada em atividades com status CONCLUIDA");
            }
        }
        atividade.setStatus(dto.status());
        return atividadeMapper.toResponse(atividadeRepository.save(atividade));
    }

    @Transactional
    public void delete(Long id) {
        Atividade atividade = findAtividadeById(id);
        atividade.setActive(false);
        atividadeRepository.save(atividade);
    }

    private void validarTipoDescricao(TipoAtividade tipo, String tipoDescricao) {
        if (tipo == TipoAtividade.OUTROS && (tipoDescricao == null || tipoDescricao.isBlank())) {
            throw new BusinessException("Campo 'tipoDescricao' é obrigatório quando o tipo for OUTROS");
        }
    }

    private void validarDataConclusao(LocalDate dataConclusao) {
        if (dataConclusao != null && dataConclusao.isAfter(LocalDate.now())) {
            throw new BusinessException("A data de conclusão não pode ser uma data futura");
        }
    }

    private StatusAtividade resolverStatus(LocalDate data, StatusAtividade statusSolicitado) {
        if (data != null && data.isAfter(LocalDate.now())) {
            return StatusAtividade.PENDENTE;
        }
        return statusSolicitado;
    }

    private Atividade findAtividadeById(Long id) {
        return atividadeRepository.findByIdAndActiveTrue(id)
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
