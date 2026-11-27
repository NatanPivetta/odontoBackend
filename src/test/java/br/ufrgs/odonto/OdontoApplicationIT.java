package br.ufrgs.odonto;

import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.enums.RoleName;
import br.ufrgs.odonto.modules.atividade.entity.Atividade;
import br.ufrgs.odonto.modules.atividade.entity.Feedback;
import br.ufrgs.odonto.modules.atividade.entity.StatusAtividade;
import br.ufrgs.odonto.modules.atividade.repository.AtividadeRepository;
import br.ufrgs.odonto.modules.atividade.repository.AtividadeSpecification;
import br.ufrgs.odonto.modules.atividade.repository.FeedbackRepository;
import br.ufrgs.odonto.modules.core.entity.EmailVerificationCode;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.EmailVerificationCodeRepository;
import br.ufrgs.odonto.modules.core.repository.SecurityRoleRepository;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import br.ufrgs.odonto.modules.turma.entity.DisciplinaClinica;
import br.ufrgs.odonto.modules.turma.entity.Turma;
import br.ufrgs.odonto.modules.turma.entity.TurmaAluno;
import br.ufrgs.odonto.modules.turma.entity.TurmaAlunoId;
import br.ufrgs.odonto.modules.turma.repository.TurmaAlunoRepository;
import br.ufrgs.odonto.modules.turma.repository.TurmaRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class OdontoApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private EmailVerificationCodeRepository codes;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TurmaRepository turmas;

    @Autowired
    private TurmaAlunoRepository matriculas;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AtividadeRepository atividades;

    @Autowired
    private FeedbackRepository feedbacks;

    @Autowired
    private SecurityRoleRepository perfis;

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @MockitoBean
    private JavaMailSender mailSender;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
    }

    @Test
    @DisplayName("C07 — Sobe a aplicação após aplicar as migrations no PostgreSQL vazio")
    void contextLoadsWithMigratedDatabase() {
        // O Spring só executa este método após Liquibase e Hibernate validarem o schema.
    }

    @Test
    @DisplayName("C01 — Primeiro acesso consome o código e permite login")
    void primeiroAcessoPersisteAlunoEPermiteLogin() throws Exception {
        String email = "primeiro-acesso@example.invalid";
        String cartao = "91234567";
        String senha = "senha-de-teste";

        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        mvc.perform(post("/api/auth/verify/send").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Aluno de teste","email":"%s","cardNumber":"%s"}
                                """.formatted(email, cartao)))
                .andExpect(status().isNoContent());

        var codigo = codes.findTopByEmailOrderByCreatedAtDesc(email).orElseThrow();
        assertThat(codigo.isUsed()).isFalse();
        assertThat(codigo.getCode()).matches("[0-9]{6}");
        assertThat(users.findByEmail(email)).isEmpty();

        mvc.perform(post("/api/auth/register").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Aluno de teste","email":"%s","cardNumber":"%s",
                                 "password":"%s","code":"%s"}
                                """.formatted(email, cartao, senha, codigo.getCode())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        var aluno = users.findByEmail(email).orElseThrow();
        assertThat(aluno.getCardNumber()).isEqualTo(cartao);
        assertThat(aluno.getRole()).isEqualTo(Role.ALUNO);
        assertThat(aluno.getPasswordHash()).isNotEqualTo(senha);
        assertThat(passwordEncoder.matches(senha, aluno.getPasswordHash())).isTrue();
        assertThat(codes.findById(codigo.getId()).orElseThrow().isUsed()).isTrue();

        mvc.perform(post("/api/auth/login").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s","password":"%s"}
                                """.formatted(cartao, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(cartao))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
    @Test
    @DisplayName("C01 — Senha incorreta é recusada para um aluno cadastrado")
    void rejeitaSenhaIncorretaSemEmitirToken() throws Exception {
        users.saveAndFlush(User.builder()
                .name("Aluno de teste")
                .email("login-negativo@example.invalid")
                .cardNumber("91234568")
                .passwordHash(passwordEncoder.encode("senha-correta"))
                .role(Role.ALUNO)
                .build());

        mvc.perform(post("/api/auth/login").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"91234568","password":"senha-errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ERROR_AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @ParameterizedTest(name = "C01 — Cadastro recusado: {4}")
    @CsvSource({
            "expirado, 91234569, -10, 123456, ERROR_VERIFICATION_CODE_EXPIRED",
            "incorreto, 91234570, 10, 654321, ERROR_VERIFICATION_CODE_INVALID"
    })
    void rejeitaCodigoInutilizavelSemCriarAluno(
            String caso, String cartao, long minutos, String codigoInformado, String erro) throws Exception {
        String email = "codigo-" + caso + "@example.invalid";
        var agora = LocalDateTime.now();
        var codigo = codes.saveAndFlush(EmailVerificationCode.builder()
                .email(email)
                .code("123456")
                .createdAt(agora.minusMinutes(20))
                .expiresAt(agora.plusMinutes(minutos))
                .used(false)
                .build());

        mvc.perform(post("/api/auth/register").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Aluno de teste","email":"%s","cardNumber":"%s",
                                 "password":"senha-de-teste","code":"%s"}
                                """.formatted(email, cartao, codigoInformado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(erro))
                .andExpect(jsonPath("$.token").doesNotExist());

        assertThat(users.findByEmail(email)).isEmpty();
        assertThat(codes.findById(codigo.getId()).orElseThrow().isUsed()).isFalse();
    }

    @Test
    @DisplayName("C07 — Cadastro incompleto é recusado sem persistir usuário")
    void rejeitaCadastroSemCamposObrigatorios() throws Exception {
        String email = "cadastro-incompleto@example.invalid";

        mvc.perform(post("/api/auth/register").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Aluno de teste","email":"%s","cardNumber":"91234571"}
                                """.formatted(email)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERROR_VALIDATION"))
                .andExpect(jsonPath("$.fields.password").exists())
                .andExpect(jsonPath("$.fields.code").exists());

        assertThat(users.findByEmail(email)).isEmpty();
    }

    @Test
    @WithMockUser(authorities = "CLASS_MANAGE")
    @DisplayName("C04 — Matrícula, duplicidade, transferência e rematrícula")
    void movimentaMatriculaPelaApi() throws Exception {
        var aluno = criaAluno("91234572");
        var origem = criaTurma("VVS-ORIGEM");
        var destino = criaTurma("VVS-DESTINO");
        var vinculoOrigem = new TurmaAlunoId(origem.getId(), aluno.getId());
        var vinculoDestino = new TurmaAlunoId(destino.getId(), aluno.getId());
        String urlOrigem = "/api/v1/turmas/%d/alunos/%d".formatted(origem.getId(), aluno.getId());
        String urlDestino = "/api/v1/turmas/%d/alunos/%d".formatted(destino.getId(), aluno.getId());

        mvc.perform(post(urlOrigem).contextPath("/api")).andExpect(status().isOk());
        assertThat(matriculas.findById(vinculoOrigem).orElseThrow().isActive()).isTrue();

        mvc.perform(post(urlOrigem).contextPath("/api"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERROR_STUDENT_ALREADY_ENROLLED"));
        assertThat(matriculas.findById(vinculoOrigem).orElseThrow().isActive()).isTrue();

        mvc.perform(post(urlDestino).contextPath("/api")).andExpect(status().isOk());
        assertThat(matriculas.findById(vinculoOrigem).orElseThrow().isActive()).isFalse();
        assertThat(matriculas.findById(vinculoDestino).orElseThrow().isActive()).isTrue();

        mvc.perform(delete(urlDestino).contextPath("/api")).andExpect(status().isNoContent());
        assertThat(matriculas.findById(vinculoDestino)).isEmpty();

        mvc.perform(post(urlOrigem).contextPath("/api")).andExpect(status().isOk());
        assertThat(matriculas.findById(vinculoOrigem).orElseThrow().isActive()).isTrue();
    }

    @Test
    @WithMockUser(authorities = "CLASS_MANAGE")
    @DisplayName("C04 — Falha persistindo o destino reverte a transferência em lote")
    void reverteTransferenciaEmLoteQuandoPostgresRecusaDestino() throws Exception {
        var aluno = criaAluno("91234573");
        var origem = criaTurma("VVS-RB-ORIGEM");
        var destino = criaTurma("VVS-RB-DESTINO");
        var vinculoOrigem = new TurmaAlunoId(origem.getId(), aluno.getId());
        var vinculoDestino = new TurmaAlunoId(destino.getId(), aluno.getId());

        mvc.perform(post("/api/v1/turmas/{id}/alunos/{alunoId}", origem.getId(), aluno.getId())
                        .contextPath("/api"))
                .andExpect(status().isOk());

        jdbc.execute("""
                ALTER TABLE turma_alunos ADD CONSTRAINT vvs_bloqueia_destino
                CHECK (turma_id <> %d OR aluno_id <> %d)
                """.formatted(destino.getId(), aluno.getId()));
        try {
            mvc.perform(post("/api/v1/turmas/{id}/alunos/bulk", destino.getId())
                            .contextPath("/api")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"alunoIds":[%d]}
                                    """.formatted(aluno.getId())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(result -> assertThat(result.getResolvedException())
                            .hasStackTraceContaining("vvs_bloqueia_destino"));

            assertThat(matriculas.findById(vinculoOrigem).orElseThrow().isActive()).isTrue();
            assertThat(matriculas.findById(vinculoDestino)).isEmpty();
        } finally {
            jdbc.execute("ALTER TABLE turma_alunos DROP CONSTRAINT vvs_bloqueia_destino");
        }
    }

    @Test
    @DisplayName("C05 — Filtra, ordena e pagina atividades no PostgreSQL")
    void consultaAtividadesComFiltrosCombinados() {
        var aluno = criaAluno("91234574");
        var outroAluno = criaAluno("91234575");
        var turma = criaTurma("VVS-CONSULTA");
        var outraTurma = criaTurma("VVS-OUTRA");
        var orientador = users.saveAndFlush(User.builder()
                .name("Professor de integração")
                .email("orientador-c05@example.invalid")
                .cardNumber("91234576")
                .passwordHash(passwordEncoder.encode("senha-de-teste"))
                .role(Role.PROFESSOR)
                .build());
        var hoje = LocalDate.now();
        var antiga = criaAtividade(aluno, turma, orientador, hoje.minusDays(2), true);
        var recente = criaAtividade(aluno, turma, orientador, hoje.minusDays(1), true);
        var inativa = criaAtividade(aluno, turma, orientador, hoje, false);
        criaAtividade(outroAluno, turma, orientador, hoje, true);
        criaAtividade(aluno, outraTurma, orientador, hoje, true);

        var filtro = AtividadeSpecification.isActive()
                .and(AtividadeSpecification.byAlunoId(aluno.getId()))
                .and(AtividadeSpecification.byTurmaId(turma.getId()));
        var ordenacao = Sort.by(Sort.Direction.DESC, "data");
        var primeiraPagina = atividades.findAll(filtro, PageRequest.of(0, 1, ordenacao));
        var segundaPagina = atividades.findAll(filtro, PageRequest.of(1, 1, ordenacao));

        assertThat(primeiraPagina.getTotalElements()).isEqualTo(2);
        assertThat(primeiraPagina.getContent()).extracting(Atividade::getId)
                .containsExactly(recente.getId());
        assertThat(segundaPagina.getContent()).extracting(Atividade::getId)
                .containsExactly(antiga.getId());

        var semResultados = filtro.and(AtividadeSpecification.byStatus(StatusAtividade.ALTA));
        assertThat(atividades.findAll(semResultados, PageRequest.of(0, 1, ordenacao))).isEmpty();
        assertThat(atividades.findByIdAndActiveTrue(inativa.getId())).isEmpty();
    }

    @Test
    @DisplayName("C05 — Consultas de turmas exigem turma e matrícula ativas")
    void consultaTurmaSomenteComVinculoAtivo() {
        var aluno = criaAluno("91234577");
        var elegivel = criaTurma("VVS-ELEGIVEL");
        var semVinculoAtivo = criaTurma("VVS-VINC-INATIVO");
        var fechada = criaTurma("VVS-TURMA-FECHADA");
        fechada.setActive(false);
        turmas.saveAndFlush(fechada);
        salvaVinculo(elegivel, aluno, true);
        salvaVinculo(semVinculoAtivo, aluno, false);
        salvaVinculo(fechada, aluno, true);

        assertThat(turmas.findActiveTurmaForAluno(aluno))
                .map(Turma::getId).contains(elegivel.getId());
        assertThat(turmas.findActiveTurmasForAlunoId(aluno.getId()))
                .extracting(Turma::getId).containsExactly(elegivel.getId());
    }

    @Test
    @DisplayName("C02 — JWT e permissões controlam acesso à API de turmas")
    void protegeConsultaEMatriculaComPermissoesDoBanco() throws Exception {
        var aluno = criaAluno("91234578");
        aluno.getRoles().add(perfis.findByName(RoleName.ALUNO).orElseThrow());
        users.saveAndFlush(aluno);
        var turma = criaTurma("VVS-C02");
        String consulta = "/api/v1/turmas/%d".formatted(turma.getId());
        String matricula = consulta + "/alunos/" + aluno.getId();

        mvc.perform(get(consulta).contextPath("/api"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(consulta).contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());

        var login = mvc.perform(post("/api/auth/login").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"91234578","password":"senha-de-teste"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(login.getResponse().getContentAsString(), "$.token");

        mvc.perform(get(consulta).contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(turma.getId().intValue()));

        mvc.perform(post(matricula).contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ERROR_ACCESS_DENIED"));
        assertThat(matriculas.findById(new TurmaAlunoId(turma.getId(), aluno.getId()))).isEmpty();
    }

    @Test
    @DisplayName("C03 — Criação, consulta, edição e exclusão lógica de atividade")
    void gerenciaAtividadePelaApi() throws Exception {
        var aluno = criaAluno("91234579");
        aluno.getRoles().add(perfis.findByName(RoleName.ALUNO).orElseThrow());
        users.saveAndFlush(aluno);
        var outroAluno = criaAluno("91234580");
        outroAluno.getRoles().add(perfis.findByName(RoleName.ALUNO).orElseThrow());
        users.saveAndFlush(outroAluno);
        var professor = User.builder()
                .name("Professor de integração")
                .email("orientador-c03@example.invalid")
                .cardNumber("91234581")
                .passwordHash(passwordEncoder.encode("senha-de-teste"))
                .role(Role.PROFESSOR)
                .build();
        professor.getRoles().add(perfis.findByName(RoleName.PROFESSOR).orElseThrow());
        users.saveAndFlush(professor);
        var turma = criaTurma("VVS-C03");
        salvaVinculo(turma, aluno, true);

        String sessaoAluno = autenticacaoPara(aluno);
        String sessaoOutroAluno = autenticacaoPara(outroAluno);
        String sessaoProfessor = autenticacaoPara(professor);
        var criacao = mvc.perform(post("/api/v1/atividades/aluno").contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, sessaoAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"data":"%s","prontuario":"VVS-C03","tipo":"ATF","professorOrientadorId":%d}
                                """.formatted(LocalDate.now().minusDays(1), professor.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prontuario").value("VVS-C03"))
                .andReturn();
        Number id = com.jayway.jsonpath.JsonPath.read(criacao.getResponse().getContentAsString(), "$.id");
        long atividadeId = id.longValue();
        String url = "/api/v1/atividades/" + atividadeId;
        assertThat(atividades.findByIdAndActiveTrue(atividadeId).orElseThrow().getProntuario())
                .isEqualTo("VVS-C03");

        mvc.perform(get(url).contextPath("/api").header(HttpHeaders.AUTHORIZATION, sessaoAluno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prontuario").value("VVS-C03"));
        mvc.perform(get(url).contextPath("/api").header(HttpHeaders.AUTHORIZATION, sessaoOutroAluno))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERROR_ACTIVITY_NOT_FOUND"));
        mvc.perform(patch(url).contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, sessaoOutroAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"observacoes":"Alteração de outro aluno"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERROR_ACTIVITY_NOT_FOUND"));
        assertThat(atividades.findById(atividadeId).orElseThrow().getObservacoes()).isNull();

        mvc.perform(patch(url).contextPath("/api").header(HttpHeaders.AUTHORIZATION, sessaoAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"observacoes":"Retorno agendado"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacoes").value("Retorno agendado"));
        assertThat(atividades.findById(atividadeId).orElseThrow().getObservacoes())
                .isEqualTo("Retorno agendado");

        mvc.perform(delete(url).contextPath("/api").header(HttpHeaders.AUTHORIZATION, sessaoProfessor))
                .andExpect(status().isNoContent());
        assertThat(atividades.findById(atividadeId).orElseThrow().isActive()).isFalse();
        mvc.perform(get(url).contextPath("/api").header(HttpHeaders.AUTHORIZATION, sessaoAluno))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("C06 — Alta e feedback respeitam as regras e o vínculo da atividade")
    void concedeAltaEPublicaFeedbackPelaApi() throws Exception {
        var aluno = criaAlunoComPerfil("91234582");
        var outroAluno = criaAlunoComPerfil("91234583");
        var professor = User.builder()
                .name("Professor C06")
                .email("orientador-c06@example.invalid")
                .cardNumber("91234584")
                .passwordHash(passwordEncoder.encode("senha-de-teste"))
                .role(Role.PROFESSOR)
                .build();
        professor.getRoles().add(perfis.findByName(RoleName.PROFESSOR).orElseThrow());
        professor = users.saveAndFlush(professor);
        var turma = criaTurma("VVS-C06");
        var atividade = criaAtividade(aluno, turma, professor, LocalDate.now().minusDays(1), true);
        atividade.setStatus(StatusAtividade.CONCLUIDA);
        atividade.setDataConclusao(atividade.getData());
        atividades.saveAndFlush(atividade);

        String sessaoAluno = autenticacaoPara(aluno);
        String sessaoOutroAluno = autenticacaoPara(outroAluno);
        String sessaoProfessor = autenticacaoPara(professor);
        String url = "/api/v1/atividades/" + atividade.getId();
        String alta = """
                {"status":"ALTA"}
                """;

        mvc.perform(patch(url + "/status").contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, sessaoAluno)
                        .contentType(MediaType.APPLICATION_JSON).content(alta))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERROR_ACTIVITY_ALTA_PROFESSOR_ONLY"));
        assertThat(atividades.findById(atividade.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusAtividade.CONCLUIDA);

        mvc.perform(patch(url + "/status").contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, sessaoProfessor)
                        .contentType(MediaType.APPLICATION_JSON).content(alta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALTA"));
        assertThat(atividades.findById(atividade.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusAtividade.ALTA);

        mvc.perform(post(url + "/feedbacks").contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, sessaoProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"texto":"Revisar a evolução"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.texto").value("Revisar a evolução"));
        assertThat(feedbacks.findByAtividadeId(atividade.getId()))
                .extracting(Feedback::getTexto).containsExactly("Revisar a evolução");

        mvc.perform(get(url + "/feedbacks").contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, sessaoAluno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].texto").value("Revisar a evolução"));
        mvc.perform(get(url + "/feedbacks").contextPath("/api")
                        .header(HttpHeaders.AUTHORIZATION, sessaoOutroAluno))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERROR_ACTIVITY_NOT_FOUND"));
    }

    @Test
    @WithMockUser(authorities = "ACTIVITY_UPDATE_ANY")
    @DisplayName("C07 — Valida status e ID em uma alteração de atividade")
    void rejeitaStatusAusenteInvalidoEAtividadeInexistente() throws Exception {
        String url = "/api/v1/atividades/" + Long.MAX_VALUE + "/status";

        mvc.perform(patch(url).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fields.status").exists());

        mvc.perform(patch(url).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"STATUS_INEXISTENTE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERROR_VALIDATION"));

        mvc.perform(patch(url).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONCLUIDA"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERROR_ACTIVITY_NOT_FOUND"));
    }

    private User criaAlunoComPerfil(String cartao) {
        var aluno = criaAluno(cartao);
        aluno.getRoles().add(perfis.findByName(RoleName.ALUNO).orElseThrow());
        return users.saveAndFlush(aluno);
    }

    private String autenticacaoPara(User usuario) throws Exception {
        var login = mvc.perform(post("/api/auth/login").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s","password":"senha-de-teste"}
                                """.formatted(usuario.getCardNumber())))
                .andExpect(status().isOk())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        return "Bearer " + token;
    }

    private Atividade criaAtividade(
            User aluno, Turma turma, User orientador, LocalDate data, boolean ativa) {
        return atividades.saveAndFlush(Atividade.builder()
                .aluno(aluno)
                .turma(turma)
                .professorOrientador(orientador)
                .data(data)
                .prontuario("VVS-C05")
                .active(ativa)
                .build());
    }

    private void salvaVinculo(Turma turma, User aluno, boolean ativo) {
        matriculas.saveAndFlush(TurmaAluno.builder()
                .id(new TurmaAlunoId(turma.getId(), aluno.getId()))
                .turma(turma)
                .aluno(aluno)
                .active(ativo)
                .build());
    }

    private User criaAluno(String cartao) {
        return users.saveAndFlush(User.builder()
                .name("Aluno de integração")
                .email("aluno-" + cartao + "@example.invalid")
                .cardNumber(cartao)
                .passwordHash(passwordEncoder.encode("senha-de-teste"))
                .role(Role.ALUNO)
                .build());
    }

    private Turma criaTurma(String codigo) {
        return turmas.saveAndFlush(Turma.builder()
                .disciplina(DisciplinaClinica.ODO99012)
                .codigoTurma(codigo)
                .name("Turma " + codigo)
                .semester("2026/2")
                .build());
    }
}
