package br.ufrgs.odonto.modules.core.resource.v1;

import br.ufrgs.odonto.dto.user.CreateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.modules.core.service.ProfessorService;
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
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/v1/professores")
@RequiredArgsConstructor
@Tag(name = "Professores")
public class ProfessorResource {

    private final ProfessorService professorService;

    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Criar professor",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "201", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public UserResponseDTO create(@Valid @RequestBody CreateProfessorRequestDTO request) {
        return professorService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROFESSOR_VIEW')")
    @Operation(
            summary = "Listar professores",
            description = "Retorna todos os usuários com papel PROFESSOR. Use o parâmetro `active` para filtrar por status.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public Page<UserResponseDTO> findAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return professorService.findAll(active, PageRequest.of(page, size, Sort.by(direction, sort)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROFESSOR_VIEW')")
    @Operation(summary = "Buscar professor por ID",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public UserResponseDTO findById(@PathVariable Long id) {
        return professorService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Atualizar professor",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public UserResponseDTO update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateProfessorRequestDTO request) {
        return professorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Desativar professor",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "204")})
    public void delete(@PathVariable Long id) {
        professorService.delete(id);
    }
}
