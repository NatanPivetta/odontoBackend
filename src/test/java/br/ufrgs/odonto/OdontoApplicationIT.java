package br.ufrgs.odonto;

import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.modules.core.repository.EmailVerificationCodeRepository;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
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
}
