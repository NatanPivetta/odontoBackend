package br.ufrgs.odonto.modules.core.resource.v1;

import br.ufrgs.odonto.dto.user.CreateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.modules.core.service.UserService;
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
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários")
public class UserResource {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Criar usuário",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "201", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public UserResponseDTO create(@Valid @RequestBody CreateUserRequestDTO request) {
        return userService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    @Operation(summary = "Listar usuários",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public Page<UserResponseDTO> findAll(@RequestParam(defaultValue = "0") Integer page,
                                        @RequestParam(defaultValue = "20") Integer size,
                                        @RequestParam(defaultValue = "name") String sort,
                                        @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return userService.findAll(PageRequest.of(page, size, Sort.by(direction, sort)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    @Operation(summary = "Buscar usuário por ID",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public UserResponseDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Atualizar usuário",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)))})
    public UserResponseDTO update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateUserRequestDTO request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Desativar usuário",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "204")})
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
