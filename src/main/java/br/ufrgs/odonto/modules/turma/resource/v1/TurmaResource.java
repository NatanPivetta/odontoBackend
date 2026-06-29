package br.ufrgs.odonto.modules.turma.resource.v1;

import br.ufrgs.odonto.dto.turma.BulkAlunoRequestDTO;
import br.ufrgs.odonto.dto.turma.CreateTurmaRequestDTO;
import br.ufrgs.odonto.dto.turma.TurmaResponseDTO;
import br.ufrgs.odonto.dto.turma.UpdateTurmaRequestDTO;
import br.ufrgs.odonto.modules.turma.service.TurmaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/v1/turmas")
@RequiredArgsConstructor
@Tag(name = "Turmas")
public class TurmaResource {

    private final TurmaService turmaService;

    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Criar turma",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "201", content = @Content(
                    schema = @Schema(implementation = TurmaResponseDTO.class)))})
    public TurmaResponseDTO create(@Valid @RequestBody CreateTurmaRequestDTO request) {
        return turmaService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Listar turmas",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = TurmaResponseDTO.class)))})
    public Page<TurmaResponseDTO> findAll(@RequestParam(defaultValue = "0") Integer page,
                                          @RequestParam(defaultValue = "20") Integer size,
                                          @RequestParam(defaultValue = "name") String sort,
                                          @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return turmaService.findAll(PageRequest.of(page, size, Sort.by(direction, sort)));
    }

    @GetMapping("/aluno/{alunoId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Listar turmas ativas de um aluno",
            description = "Retorna apenas as turmas em que o aluno possui matrícula ativa.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = TurmaResponseDTO.class)))})
    public List<TurmaResponseDTO> findTurmasDoAluno(@PathVariable Long alunoId) {
        return turmaService.findTurmasDoAluno(alunoId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Buscar turma por ID",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = TurmaResponseDTO.class)))})
    public TurmaResponseDTO findById(@PathVariable Long id) {
        return turmaService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Atualizar turma",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = TurmaResponseDTO.class)))})
    public TurmaResponseDTO update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateTurmaRequestDTO request) {
        return turmaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Desativar turma",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "204")})
    public void delete(@PathVariable Long id) {
        turmaService.delete(id);
    }

    @PostMapping("/{id}/alunos/{alunoId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Matricular aluno na turma",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = TurmaResponseDTO.class)))})
    public TurmaResponseDTO addAluno(@PathVariable Long id, @PathVariable Long alunoId) {
        return turmaService.addAluno(id, alunoId);
    }

    @DeleteMapping("/{id}/alunos/{alunoId}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Remover aluno da turma",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "204")})
    public void removeAluno(@PathVariable Long id, @PathVariable Long alunoId) {
        turmaService.removeAluno(id, alunoId);
    }

    @PostMapping("/{id}/alunos/bulk")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Matricular vários alunos na turma",
            description = "Alunos já matriculados são ignorados (idempotente).",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = TurmaResponseDTO.class)))})
    public TurmaResponseDTO addAlunosBulk(@PathVariable Long id,
                                          @Valid @RequestBody BulkAlunoRequestDTO request) {
        return turmaService.addAlunosBulk(id, request);
    }

    @DeleteMapping("/{id}/alunos/bulk")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Remover vários alunos da turma",
            description = "Alunos não matriculados são ignorados (idempotente).",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "204")})
    public void removeAlunosBulk(@PathVariable Long id,
                                  @Valid @RequestBody BulkAlunoRequestDTO request) {
        turmaService.removeAlunosBulk(id, request);
    }
}