package br.ufrgs.odonto.modules.core.resource.v1;

import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.modules.core.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/alunos")
@RequiredArgsConstructor
@Tag(name = "Alunos")
public class AlunoResource {

    private final AlunoService alunoService;

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    @Operation(
            summary = "Listar alunos",
            description = "Retorna todos os usuários com papel ALUNO. Use o parâmetro `active` para filtrar por status.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))}
    )
    public Page<UserResponseDTO> findAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return alunoService.findAll(active, PageRequest.of(page, size, Sort.by(direction, sort)));
    }
}
