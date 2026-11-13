package br.ufrgs.odonto.security;

import br.ufrgs.odonto.dto.atividade.CreateAtividadeAlunoRequestDTO;
import br.ufrgs.odonto.dto.atividade.CreateAtividadeRequestDTO;
import br.ufrgs.odonto.dto.atividade.CreateFeedbackRequestDTO;
import br.ufrgs.odonto.dto.atividade.UpdateAtividadeAlunoRequestDTO;
import br.ufrgs.odonto.dto.atividade.UpdateAtividadeRequestDTO;
import br.ufrgs.odonto.dto.atividade.UpdateStatusAtividadeRequestDTO;
import br.ufrgs.odonto.dto.turma.BulkAlunoRequestDTO;
import br.ufrgs.odonto.dto.turma.CreateTurmaRequestDTO;
import br.ufrgs.odonto.dto.turma.UpdateTurmaRequestDTO;
import br.ufrgs.odonto.dto.user.CreateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.CreateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateUserRequestDTO;
import br.ufrgs.odonto.modules.atividade.resource.v1.AtividadeResource;
import br.ufrgs.odonto.modules.atividade.resource.v1.FeedbackResource;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import br.ufrgs.odonto.modules.atividade.entity.TipoAtividade;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.resource.v1.AlunoResource;
import br.ufrgs.odonto.modules.core.resource.v1.ProfessorResource;
import br.ufrgs.odonto.modules.core.resource.v1.UserResource;
import br.ufrgs.odonto.modules.turma.resource.v1.TurmaResource;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceAuthorizationAnnotationsTest {

    private static final List<Class<?>> PROTECTED_RESOURCES = List.of(
            AtividadeResource.class,
            FeedbackResource.class,
            AlunoResource.class,
            ProfessorResource.class,
            UserResource.class,
            TurmaResource.class
    );

    @Test
    void resourcesProtegidosNaoDevemUsarHasRole() {
        for (Class<?> resource : PROTECTED_RESOURCES) {
            for (Method method : resource.getDeclaredMethods()) {
                PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

                if (preAuthorize != null) {
                    assertThat(preAuthorize.value())
                            .as("%s.%s", resource.getSimpleName(), method.getName())
                            .doesNotContain("hasRole")
                            .doesNotContain("hasAnyRole");
                }
            }
        }
    }

    @Test
    void atividadeResourceUsaPermissionsEsperadas() throws NoSuchMethodException {
        assertPreAuthorize(AtividadeResource.class, "create",
                "hasAuthority('ACTIVITY_CREATE')",
                CreateAtividadeRequestDTO.class);
        assertPreAuthorize(AtividadeResource.class, "createAsAluno",
                "hasAuthority('ACTIVITY_CREATE')",
                CreateAtividadeAlunoRequestDTO.class, User.class);
        assertPreAuthorize(AtividadeResource.class, "findAll",
                "hasAuthority('ACTIVITY_VIEW_ANY')",
                Long.class, Long.class, Long.class, Long.class, Long.class, TipoAtividade.class,
                StatusAtividade.class, Integer.class, Integer.class, String.class, Sort.Direction.class);
        assertPreAuthorize(AtividadeResource.class, "findMinhas",
                "hasAuthority('ACTIVITY_VIEW_OWN')",
                TipoAtividade.class, StatusAtividade.class, Integer.class, Integer.class, String.class, Sort.Direction.class, User.class);
        assertPreAuthorize(AtividadeResource.class, "findById",
                "hasAnyAuthority('ACTIVITY_VIEW_ANY', 'ACTIVITY_VIEW_OWN')",
                Long.class, User.class);
        assertPreAuthorize(AtividadeResource.class, "update",
                "hasAuthority('ACTIVITY_UPDATE_ANY')",
                Long.class, UpdateAtividadeRequestDTO.class);
        assertPreAuthorize(AtividadeResource.class, "updateDadosBasicos",
                "hasAuthority('ACTIVITY_UPDATE_OWN')",
                Long.class, UpdateAtividadeAlunoRequestDTO.class, User.class);
        assertPreAuthorize(AtividadeResource.class, "updateStatus",
                "hasAnyAuthority('ACTIVITY_UPDATE_ANY', 'ACTIVITY_UPDATE_OWN')",
                Long.class, UpdateStatusAtividadeRequestDTO.class, User.class);
        assertPreAuthorize(AtividadeResource.class, "delete",
                "hasAuthority('ACTIVITY_UPDATE_ANY')",
                Long.class);
    }

    @Test
    void feedbackResourceUsaPermissionsEsperadas() throws NoSuchMethodException {
        assertPreAuthorize(FeedbackResource.class, "findAll",
                "hasAnyAuthority('FEEDBACK_VIEW_ANY', 'FEEDBACK_VIEW_OWN')",
                Long.class, User.class);
        assertPreAuthorize(FeedbackResource.class, "create",
                "hasAuthority('FEEDBACK_CREATE')",
                Long.class, CreateFeedbackRequestDTO.class, User.class);
        assertPreAuthorize(FeedbackResource.class, "delete",
                "hasAuthority('FEEDBACK_CREATE')",
                Long.class, Long.class);
    }

    @Test
    void coreResourcesUsamPermissionsEsperadas() throws NoSuchMethodException {
        assertPreAuthorize(AlunoResource.class, "findAll",
                "hasAuthority('STUDENT_VIEW')",
                Boolean.class, Integer.class, Integer.class, String.class, Sort.Direction.class);

        assertPreAuthorize(ProfessorResource.class, "create",
                "hasAuthority('USER_MANAGE')",
                CreateProfessorRequestDTO.class);
        assertPreAuthorize(ProfessorResource.class, "findAll",
                "hasAuthority('PROFESSOR_VIEW')",
                Boolean.class, Integer.class, Integer.class, String.class, Sort.Direction.class);
        assertPreAuthorize(ProfessorResource.class, "findById",
                "hasAuthority('PROFESSOR_VIEW')",
                Long.class);
        assertPreAuthorize(ProfessorResource.class, "update",
                "hasAuthority('USER_MANAGE')",
                Long.class, UpdateProfessorRequestDTO.class);
        assertPreAuthorize(ProfessorResource.class, "delete",
                "hasAuthority('USER_MANAGE')",
                Long.class);

        assertPreAuthorize(UserResource.class, "create",
                "hasAuthority('USER_MANAGE')",
                CreateUserRequestDTO.class);
        assertPreAuthorize(UserResource.class, "findAll",
                "hasAuthority('USER_VIEW')",
                Integer.class, Integer.class, String.class, Sort.Direction.class);
        assertPreAuthorize(UserResource.class, "findById",
                "hasAuthority('USER_VIEW')",
                Long.class);
        assertPreAuthorize(UserResource.class, "update",
                "hasAuthority('USER_MANAGE')",
                Long.class, UpdateUserRequestDTO.class);
        assertPreAuthorize(UserResource.class, "delete",
                "hasAuthority('USER_MANAGE')",
                Long.class);
    }

    @Test
    void turmaResourceUsaPermissionsEsperadas() throws NoSuchMethodException {
        assertPreAuthorize(TurmaResource.class, "findDisciplinas",
                "hasAuthority('CLASS_VIEW')");
        assertPreAuthorize(TurmaResource.class, "create",
                "hasAuthority('CLASS_MANAGE')",
                CreateTurmaRequestDTO.class);
        assertPreAuthorize(TurmaResource.class, "findAll",
                "hasAuthority('CLASS_VIEW')",
                Integer.class, Integer.class, String.class, Sort.Direction.class);
        assertPreAuthorize(TurmaResource.class, "findTurmasDoAluno",
                "hasAuthority('CLASS_VIEW')",
                Long.class);
        assertPreAuthorize(TurmaResource.class, "findById",
                "hasAuthority('CLASS_VIEW')",
                Long.class);
        assertPreAuthorize(TurmaResource.class, "update",
                "hasAuthority('CLASS_MANAGE')",
                Long.class, UpdateTurmaRequestDTO.class);
        assertPreAuthorize(TurmaResource.class, "delete",
                "hasAuthority('CLASS_MANAGE')",
                Long.class);
        assertPreAuthorize(TurmaResource.class, "addAluno",
                "hasAuthority('CLASS_MANAGE')",
                Long.class, Long.class);
        assertPreAuthorize(TurmaResource.class, "removeAluno",
                "hasAuthority('CLASS_MANAGE')",
                Long.class, Long.class);
        assertPreAuthorize(TurmaResource.class, "addAlunosBulk",
                "hasAuthority('CLASS_MANAGE')",
                Long.class, BulkAlunoRequestDTO.class);
        assertPreAuthorize(TurmaResource.class, "removeAlunosBulk",
                "hasAuthority('CLASS_MANAGE')",
                Long.class, BulkAlunoRequestDTO.class);
    }

    private void assertPreAuthorize(
            Class<?> resource,
            String methodName,
            String expectedExpression,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        Method method = resource.getDeclaredMethod(methodName, parameterTypes);

        assertThat(method.getAnnotation(PreAuthorize.class))
                .as("%s.%s deve ter @PreAuthorize", resource.getSimpleName(), methodName)
                .isNotNull();
        assertThat(method.getAnnotation(PreAuthorize.class).value())
                .isEqualTo(expectedExpression);
    }
}
