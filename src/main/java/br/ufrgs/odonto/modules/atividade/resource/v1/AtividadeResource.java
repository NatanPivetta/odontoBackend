package br.ufrgs.odonto.modules.atividade.resource.v1;

import br.ufrgs.odonto.dto.atividade.*;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import br.ufrgs.odonto.modules.atividade.entity.TipoAtividade;
import br.ufrgs.odonto.modules.atividade.service.AtividadeService;
import br.ufrgs.odonto.modules.core.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/v1/atividades")
@RequiredArgsConstructor
@Tag(name = "Atividades")
public class AtividadeResource {

    private final AtividadeService atividadeService;

    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('ACTIVITY_CREATE')")
    @Operation(summary = "Criar atividade (professor)",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "201", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public AtividadeResponseDTO create(@Valid @RequestBody CreateAtividadeRequestDTO request) {
        return atividadeService.create(request);
    }

    @PostMapping("/aluno")
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('ACTIVITY_CREATE')")
    @Operation(summary = "Criar atividade (aluno)",
            description = "Turma inferida automaticamente da matrícula ativa do aluno.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "201", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public AtividadeResponseDTO createAsAluno(@Valid @RequestBody CreateAtividadeAlunoRequestDTO request,
                                              @AuthenticationPrincipal User loggedUser) {
        return atividadeService.createAsAluno(request, loggedUser);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACTIVITY_VIEW_ANY')")
    @Operation(summary = "Listar atividades (professor)",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public Page<AtividadeResponseDTO> findAll(
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) Long turmaId,
            @RequestParam(required = false) Long atividadePaiId,
            @RequestParam(required = false) Long professorOrientadorId,
            @RequestParam(required = false) Long professorTutorId,
            @RequestParam(required = false) TipoAtividade tipo,
            @RequestParam(required = false) StatusAtividade status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "data") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return atividadeService.findAll(
                PageRequest.of(page, size, Sort.by(direction, sort)),
                alunoId, turmaId, atividadePaiId, professorOrientadorId, professorTutorId, tipo, status);
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasAuthority('ACTIVITY_VIEW_OWN')")
    @Operation(summary = "Listar minhas atividades (aluno)",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public Page<AtividadeResponseDTO> findMinhas(
            @RequestParam(required = false) TipoAtividade tipo,
            @RequestParam(required = false) StatusAtividade status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "data") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @AuthenticationPrincipal User loggedUser) {
        return atividadeService.findMinhas(
                PageRequest.of(page, size, Sort.by(direction, sort)), loggedUser, tipo, status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ACTIVITY_VIEW_ANY', 'ACTIVITY_VIEW_OWN')")
    @Operation(summary = "Buscar atividade por ID",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public AtividadeResponseDTO findById(@PathVariable Long id,
                                         @AuthenticationPrincipal User loggedUser) {
        return atividadeService.findById(id, loggedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTIVITY_UPDATE_ANY')")
    @Operation(summary = "Atualizar atividade (professor)",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public AtividadeResponseDTO update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateAtividadeRequestDTO request) {
        return atividadeService.update(id, request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTIVITY_UPDATE_OWN')")
    @Operation(summary = "Atualizar dados básicos da atividade (aluno)",
            description = "Aluno pode atualizar apenas data, dataConclusão, prontuário, nome do paciente e observações.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public AtividadeResponseDTO updateDadosBasicos(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateAtividadeAlunoRequestDTO request,
                                                    @AuthenticationPrincipal User loggedUser) {
        return atividadeService.updateDadosBasicos(id, request, loggedUser);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ACTIVITY_UPDATE_ANY', 'ACTIVITY_UPDATE_OWN')")
    @Operation(summary = "Atualizar status da atividade",
            description = "Professor pode atualizar qualquer atividade. Aluno pode atualizar apenas as próprias.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = AtividadeResponseDTO.class)))})
    public AtividadeResponseDTO updateStatus(@PathVariable Long id,
                                              @Valid @RequestBody UpdateStatusAtividadeRequestDTO request,
                                              @AuthenticationPrincipal User loggedUser) {
        return atividadeService.updateStatus(id, request, loggedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('ACTIVITY_UPDATE_ANY')")
    @Operation(summary = "Desativar atividade (professor)",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "204")})
    public void delete(@PathVariable Long id) {
        atividadeService.delete(id);
    }
}
