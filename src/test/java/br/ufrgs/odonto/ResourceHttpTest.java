package br.ufrgs.odonto;

import br.ufrgs.odonto.config.I18nConfig;
import br.ufrgs.odonto.config.auth.JwtService;
import br.ufrgs.odonto.config.auth.SecurityConfig;
import br.ufrgs.odonto.dto.atividade.CreateFeedbackRequestDTO;
import br.ufrgs.odonto.dto.atividade.FeedbackResponseDTO;
import br.ufrgs.odonto.dto.auth.LoginRequestDTO;
import br.ufrgs.odonto.dto.auth.LoginResponseDTO;
import br.ufrgs.odonto.dto.auth.RegisterRequestDTO;
import br.ufrgs.odonto.dto.auth.SendVerificationCodeRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.atividade.resource.v1.AtividadeResource;
import br.ufrgs.odonto.modules.atividade.resource.v1.FeedbackResource;
import br.ufrgs.odonto.modules.atividade.service.AtividadeService;
import br.ufrgs.odonto.modules.atividade.service.FeedbackService;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.resource.v1.AuthResource;
import br.ufrgs.odonto.modules.core.service.AuthService;
import br.ufrgs.odonto.modules.turma.resource.v1.TurmaResource;
import br.ufrgs.odonto.modules.turma.service.TurmaService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthResource.class, TurmaResource.class, AtividadeResource.class, FeedbackResource.class})
@Import({SecurityConfig.class, JwtService.class, I18nConfig.class})
@ActiveProfiles("test")
@Tag("resource")
class ResourceHttpTest {

    private static final String TURMAS = "/api/v1/turmas";
    private static final String ATIVIDADES = "/api/v1/atividades";
    private static final String FEEDBACKS = ATIVIDADES + "/12/feedbacks";
    private static final String LOGIN = """
            {"cardNumber":"00000123","password":"senha-de-teste"}
            """;
    private static final String REGISTRO = """
            {"name":"Aluno","email":"aluno@example.invalid","cardNumber":"00000123",
             "password":"senha-de-teste","code":"012345"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwt;

    @MockitoBean
    private UserDetailsService userDetails;

    @MockitoBean
    private AuthService auth;

    @MockitoBean
    private TurmaService turmas;

    @MockitoBean
    private AtividadeService atividades;

    @MockitoBean
    private FeedbackService feedbacks;

    @Test
    void loginPublicoRetornaSessaoSemExporSenha() throws Exception {
        when(auth.login(new LoginRequestDTO("00000123", "senha-de-teste")))
                .thenReturn(new LoginResponseDTO("token-de-teste", "Aluno", "00000123", Role.ALUNO));

        mvc.perform(post("/api/auth/login").contextPath("/api").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-teste"))
                .andExpect(jsonPath("$.cardNumber").value("00000123"))
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void credenciaisInvalidasRetornam401SemToken() throws Exception {
        when(auth.login(any(LoginRequestDTO.class))).thenThrow(new BadCredentialsException("Falha de teste"));

        mvc.perform(post("/api/auth/login").contextPath("/api").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ERROR_AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginComCamposInvalidosRetorna422SemChamarServico() throws Exception {
        mvc.perform(post("/api/auth/login").contextPath("/api").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\":\"123456789\",\"password\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERROR_VALIDATION"))
                .andExpect(jsonPath("$.fields.cardNumber").exists())
                .andExpect(jsonPath("$.fields.password").exists());
        verifyNoInteractions(auth);
    }

    @Test
    void envioDeCodigoRetorna204() throws Exception {
        mvc.perform(post("/api/auth/verify/send").contextPath("/api").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Aluno","email":"aluno@example.invalid","cardNumber":"00000123"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(auth).sendVerificationCode(new SendVerificationCodeRequestDTO("Aluno", "aluno@example.invalid", "00000123"));
    }

    @Test
    void cadastroValidoRetorna201() throws Exception {
        when(auth.register(new RegisterRequestDTO("Aluno", "aluno@example.invalid", "00000123",
                "senha-de-teste", "012345")))
                .thenReturn(new LoginResponseDTO("token-de-teste", "Aluno", "00000123", Role.ALUNO));

        mvc.perform(post("/api/auth/register").contextPath("/api").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-de-teste"))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void cadastroComCodigoExpiradoRetorna409SemToken() throws Exception {
        when(auth.register(any(RegisterRequestDTO.class)))
                .thenThrow(new BusinessException(ErrorCode.ERROR_VERIFICATION_CODE_EXPIRED));

        mvc.perform(post("/api/auth/register").contextPath("/api").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERROR_VERIFICATION_CODE_EXPIRED"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void listagemProtegidaSemSessaoRetorna401() throws Exception {
        mvc.perform(get(TURMAS).contextPath("/api"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(turmas, userDetails);
    }

    @Test
    void listagemProtegidaComBearerInvalidoRetorna401() throws Exception {
        mvc.perform(get(TURMAS).contextPath("/api").header(HttpHeaders.AUTHORIZATION, "Bearer invalido"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(turmas, userDetails);
    }

    @Test
    void listagemComJwtValidoEncaminhaPaginacaoEOrdenacao() throws Exception {
        var principal = org.springframework.security.core.userdetails.User.withUsername("00000123")
                .password("hash-de-teste").authorities("CLASS_VIEW").build();
        when(userDetails.loadUserByUsername("00000123")).thenReturn(principal);
        var page = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "semester"));
        when(turmas.findAll(page)).thenReturn(Page.empty(page));
        String token = jwt.generateToken("00000123", "PROFESSOR");

        mvc.perform(get(TURMAS).contextPath("/api").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "1").param("size", "5").param("sort", "semester").param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));
        verify(turmas).findAll(page);
    }

    @Test
    @WithMockUser(authorities = "CLASS_VIEW")
    void permissaoDeConsultaNaoAutorizaMatricula() throws Exception {
        mvc.perform(post(TURMAS + "/3/alunos/7").contextPath("/api"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ERROR_ACCESS_DENIED"));
        verifyNoInteractions(turmas);
    }

    @Test
    @WithMockUser(authorities = "CLASS_MANAGE")
    void matriculaDuplicadaRetorna409() throws Exception {
        when(turmas.addAluno(3L, 7L)).thenThrow(new BusinessException(ErrorCode.ERROR_STUDENT_ALREADY_ENROLLED));

        mvc.perform(post(TURMAS + "/3/alunos/7").contextPath("/api"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERROR_STUDENT_ALREADY_ENROLLED"));
    }

    @Test
    void atividadeOcultadaPeloServicoRetorna404() throws Exception {
        var aluno = User.builder().id(7L).role(Role.ALUNO).build();
        when(atividades.findById(12L, aluno))
                .thenThrow(new ResourceNotFoundException(ErrorCode.ERROR_ACTIVITY_NOT_FOUND, 12L));

        mvc.perform(get(ATIVIDADES + "/12").contextPath("/api")
                        .with(authentication(new UsernamePasswordAuthenticationToken(aluno, null, List.of(new SimpleGrantedAuthority("ACTIVITY_VIEW_OWN"))))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERROR_ACTIVITY_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value(ATIVIDADES + "/12"));
        verify(atividades).findById(12L, aluno);
    }

    @Test
    @WithMockUser(authorities = "ACTIVITY_UPDATE_ANY")
    void exclusaoDeAtividadeRetorna204() throws Exception {
        mvc.perform(delete(ATIVIDADES + "/12").contextPath("/api"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(atividades).delete(12L);
    }

    @Test
    @WithMockUser(authorities = "FEEDBACK_CREATE")
    void feedbackEmBrancoRetorna422SemChamarServico() throws Exception {
        mvc.perform(post(FEEDBACKS).contextPath("/api").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\" \"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fields.texto").exists());
        verifyNoInteractions(feedbacks);
    }

    @Test
    void feedbackValidoEncaminhaProfessorAutenticadoERetorna201() throws Exception {
        var professor = User.builder().id(9L).role(Role.PROFESSOR).build();
        var request = new CreateFeedbackRequestDTO("Revisar a atividade");
        when(feedbacks.create(12L, request, professor))
                .thenReturn(new FeedbackResponseDTO(15L, request.texto(), null, null));

        mvc.perform(post(FEEDBACKS).contextPath("/api")
                        .with(authentication(new UsernamePasswordAuthenticationToken(professor, null, List.of(new SimpleGrantedAuthority("FEEDBACK_CREATE")))))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Revisar a atividade\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.texto").value(request.texto()));
        verify(feedbacks).create(12L, request, professor);
    }
}
