package br.ufrgs.odonto;

import br.ufrgs.odonto.config.I18nConfig;
import br.ufrgs.odonto.config.auth.JwtService;
import br.ufrgs.odonto.config.auth.SecurityConfig;
import br.ufrgs.odonto.dto.user.CreateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.CreateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.core.resource.v1.AlunoResource;
import br.ufrgs.odonto.modules.core.resource.v1.ProfessorResource;
import br.ufrgs.odonto.modules.core.resource.v1.UserResource;
import br.ufrgs.odonto.modules.core.service.AlunoService;
import br.ufrgs.odonto.modules.core.service.ProfessorService;
import br.ufrgs.odonto.modules.core.service.UserService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserResource.class, ProfessorResource.class, AlunoResource.class})
@Import({SecurityConfig.class, JwtService.class, I18nConfig.class})
@ActiveProfiles("test")
@Tag("resource")
class AdministrativeResourceHttpTest {

    private static final String USERS = "/api/v1/users";
    private static final String PROFESSORES = "/api/v1/professores";
    private static final String ALUNOS = "/api/v1/alunos";
    private static final String CREATE_USER = """
            {"name":"Aluno","email":"aluno@example.invalid","cardNumber":"00000123",
             "password":"senha-de-teste","role":"ALUNO"}
            """;
    private static final String CREATE_PROFESSOR = """
            {"name":"Professor","email":"professor@example.invalid","cardNumber":"00000456",
             "password":"senha-de-teste"}
            """;
    private static final UserResponseDTO ALUNO =
            new UserResponseDTO(7L, "Aluno", "aluno@example.invalid", "00000123", Role.ALUNO, true, null);
    private static final UserResponseDTO PROFESSOR =
            new UserResponseDTO(9L, "Professor", "professor@example.invalid", "00000456", Role.PROFESSOR, true, null);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserDetailsService userDetails;

    @MockitoBean
    private UserService users;

    @MockitoBean
    private ProfessorService professores;

    @MockitoBean
    private AlunoService alunos;

    @Test
    @WithMockUser(authorities = "USER_MANAGE")
    void criaUsuarioSemExporSenha() throws Exception {
        var request = new CreateUserRequestDTO("Aluno", "aluno@example.invalid", "00000123", "senha-de-teste", Role.ALUNO);
        when(users.create(request)).thenReturn(ALUNO);

        mvc.perform(post(USERS).contextPath("/api").contentType(MediaType.APPLICATION_JSON).content(CREATE_USER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGE")
    void cadastroSemPapelRetorna422() throws Exception {
        mvc.perform(post(USERS).contextPath("/api").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Aluno","email":"aluno@example.invalid","cardNumber":"00000123","password":"senha-de-teste"}
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fields.role").exists());
        verifyNoInteractions(users);
    }

    @Test
    @WithMockUser(authorities = "USER_VIEW")
    void listaUsuariosComPaginacaoPadrao() throws Exception {
        var page = PageRequest.of(0, 20, Sort.by("name"));
        when(users.findAll(page)).thenReturn(new PageImpl<>(List.of(ALUNO), page, 1));

        mvc.perform(get(USERS).contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.totalElements").value(1));
        verify(users).findAll(page);
    }

    @Test
    @WithMockUser(authorities = "USER_VIEW")
    void usuarioInexistenteRetorna404() throws Exception {
        when(users.findById(7L)).thenThrow(new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND, 7L));

        mvc.perform(get(USERS + "/7").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERROR_USER_NOT_FOUND"));
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGE")
    void atualizaSomenteOsDadosInformadosDoUsuario() throws Exception {
        var request = new UpdateUserRequestDTO(null, null, null, false);
        when(users.update(7L, request)).thenReturn(new UserResponseDTO(
                7L, ALUNO.name(), ALUNO.email(), ALUNO.cardNumber(), Role.ALUNO, false, null));

        mvc.perform(put(USERS + "/7").contextPath("/api").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        verify(users).update(7L, request);
    }

    @Test
    @WithMockUser(authorities = "USER_VIEW")
    void consultaDeUsuarioNaoAutorizaDesativacao() throws Exception {
        mvc.perform(delete(USERS + "/7").contextPath("/api"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(users);
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGE")
    void desativaUsuarioERetorna204() throws Exception {
        mvc.perform(delete(USERS + "/7").contextPath("/api"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(users).delete(7L);
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGE")
    void criaProfessorComAutoridadeDeGestao() throws Exception {
        var request = new CreateProfessorRequestDTO("Professor", "professor@example.invalid", "00000456", "senha-de-teste");
        when(professores.create(request)).thenReturn(PROFESSOR);

        mvc.perform(post(PROFESSORES).contextPath("/api").contentType(MediaType.APPLICATION_JSON).content(CREATE_PROFESSOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("PROFESSOR"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "PROFESSOR_VIEW")
    void listaProfessoresInativosComOsParametrosSolicitados() throws Exception {
        var page = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "email"));
        var inativo = new UserResponseDTO(9L, PROFESSOR.name(), PROFESSOR.email(), PROFESSOR.cardNumber(),
                Role.PROFESSOR, false, null);
        when(professores.findAll(false, page)).thenReturn(new PageImpl<>(List.of(inativo), page, 6));

        mvc.perform(get(PROFESSORES).contextPath("/api").param("active", "false").param("page", "1")
                        .param("size", "5").param("direction", "DESC").param("sort", "email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].active").value(false))
                .andExpect(jsonPath("$.totalElements").value(6));
        verify(professores).findAll(false, page);
    }

    @Test
    @WithMockUser(authorities = "PROFESSOR_VIEW")
    void consultaProfessorPorId() throws Exception {
        when(professores.findById(9L)).thenReturn(PROFESSOR);

        mvc.perform(get(PROFESSORES + "/9").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.role").value("PROFESSOR"));
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGE")
    void emailDuplicadoNaEdicaoDeProfessorRetorna409() throws Exception {
        when(professores.update(9L, new UpdateProfessorRequestDTO(null, "ocupado@example.invalid", null)))
                .thenThrow(new BusinessException(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED));

        mvc.perform(put(PROFESSORES + "/9").contextPath("/api").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ocupado@example.invalid"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERROR_EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGE")
    void desativaProfessorERetorna204() throws Exception {
        mvc.perform(delete(PROFESSORES + "/9").contextPath("/api"))
                .andExpect(status().isNoContent());
        verify(professores).delete(9L);
    }

    @Test
    @WithMockUser(authorities = "STUDENT_VIEW")
    void listaAlunosAtivosEEncaminhaFiltro() throws Exception {
        var page = PageRequest.of(0, 20, Sort.by("name"));
        when(alunos.findAll(true, page)).thenReturn(new PageImpl<>(List.of(ALUNO), page, 1));

        mvc.perform(get(ALUNOS).contextPath("/api").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").value("ALUNO"))
                .andExpect(jsonPath("$.content[0].active").value(true));
        verify(alunos).findAll(true, page);
    }

    @ParameterizedTest
    @ValueSource(strings = {USERS, PROFESSORES, ALUNOS})
    @WithMockUser(authorities = "CLASS_VIEW")
    void autoridadeDeTurmasNaoAutorizaListagensAdministrativas(String endpoint) throws Exception {
        mvc.perform(get(endpoint).contextPath("/api"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ERROR_ACCESS_DENIED"));
        verifyNoInteractions(users, professores, alunos);
    }
}
