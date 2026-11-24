package br.ufrgs.odonto.modules.atividade.resource.v1;

import br.ufrgs.odonto.dto.atividade.CreateFeedbackRequestDTO;
import br.ufrgs.odonto.dto.atividade.FeedbackResponseDTO;
import br.ufrgs.odonto.modules.atividade.service.FeedbackService;
import br.ufrgs.odonto.modules.core.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/v1/atividades/{atividadeId}/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedbacks")
public class FeedbackResource {

    private final FeedbackService feedbackService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FEEDBACK_VIEW_ANY', 'FEEDBACK_VIEW_OWN')")
    @Operation(summary = "Listar feedbacks da atividade",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = FeedbackResponseDTO.class)))})
    public List<FeedbackResponseDTO> findAll(@PathVariable Long atividadeId,
                                              @AuthenticationPrincipal User loggedUser) {
        return feedbackService.findAll(atividadeId, loggedUser);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('FEEDBACK_CREATE')")
    @Operation(summary = "Adicionar feedback à atividade",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "201", content = @Content(
                    schema = @Schema(implementation = FeedbackResponseDTO.class)))})
    public FeedbackResponseDTO create(@PathVariable Long atividadeId,
                                      @Valid @RequestBody CreateFeedbackRequestDTO request,
                                      @AuthenticationPrincipal User loggedUser) {
        return feedbackService.create(atividadeId, request, loggedUser);
    }

    @DeleteMapping("/{feedbackId}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('FEEDBACK_CREATE')")
    @Operation(summary = "Remover feedback da atividade",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {@ApiResponse(responseCode = "204")})
    public void delete(@PathVariable Long atividadeId, @PathVariable Long feedbackId) {
        feedbackService.delete(atividadeId, feedbackId);
    }
}
