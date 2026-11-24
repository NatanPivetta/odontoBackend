package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.config.auth.JwtService;
import br.ufrgs.odonto.dto.auth.LoginRequestDTO;
import br.ufrgs.odonto.dto.auth.LoginResponseDTO;
import br.ufrgs.odonto.dto.auth.RegisterRequestDTO;
import br.ufrgs.odonto.dto.auth.SendVerificationCodeRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.modules.core.entity.EmailVerificationCode;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.EmailVerificationCodeRepository;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private static final String EMAIL = "aluno@example.invalid";
    private static final String CARD = "00000123";
    private static final String PASSWORD = "senha-de-teste";
    private static final String CODE = "012345";

    private final AuthenticationManager authentication = mock(AuthenticationManager.class);
    private final JwtService jwt = mock(JwtService.class);
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final EmailVerificationCodeRepository codes = mock(EmailVerificationCodeRepository.class);
    private final EmailService email = mock(EmailService.class);
    private final AuthService service = new AuthService(
            authentication, jwt, users, encoder, codes, email, new CardNumberNormalizer());

    @Test
    void loginAutenticaCartaoNormalizadoEEmiteTokenParaUsuarioAutenticado() {
        User user = User.builder().name("Professor").cardNumber(CARD).role(Role.PROFESSOR).build();
        when(authentication.authenticate(new UsernamePasswordAuthenticationToken(CARD, PASSWORD)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwt.generateToken(CARD, "PROFESSOR")).thenReturn("token");

        assertThat(service.login(new LoginRequestDTO("1-23", PASSWORD)))
                .isEqualTo(new LoginResponseDTO("token", "Professor", CARD, Role.PROFESSOR));
    }

    @Test
    void loginComSenhaInvalidaNaoEmiteToken() {
        when(authentication.authenticate(new UsernamePasswordAuthenticationToken(CARD, PASSWORD)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> service.login(new LoginRequestDTO("123", PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwt);
    }

    @Test
    void enviaOMesmoCodigoPersistidoComValidadeDeQuinzeMinutos() {
        service.sendVerificationCode(new SendVerificationCodeRequestDTO("Aluno", EMAIL, "123"));

        var captor = ArgumentCaptor.forClass(EmailVerificationCode.class);
        var ordem = inOrder(codes, email);
        ordem.verify(codes).deleteByEmail(EMAIL);
        ordem.verify(codes).save(captor.capture());
        var verification = captor.getValue();
        assertThat(verification.getEmail()).isEqualTo(EMAIL);
        assertThat(verification.getCode()).matches("[0-9]{6}");
        assertThat(verification.isUsed()).isFalse();
        assertThat(verification.getExpiresAt())
                .isCloseTo(verification.getCreatedAt().plusMinutes(15), within(1, java.time.temporal.ChronoUnit.SECONDS));
        ordem.verify(email).sendVerificationCode(EMAIL, verification.getCode());
    }

    @Test
    void propagaFalhaDeEnvioSemCriarUsuarioOuEmitirToken() {
        var falha = new MailSendException("SMTP de teste indisponível");
        doThrow(falha).when(email).sendVerificationCode(eq(EMAIL), anyString());

        assertThatThrownBy(() -> service.sendVerificationCode(new SendVerificationCodeRequestDTO("Aluno", EMAIL, "123")))
                .isSameAs(falha);
        verify(users, never()).save(any(User.class));
        verifyNoInteractions(jwt);
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {
            "ERROR_EMAIL_ALREADY_REGISTERED", "ERROR_CARD_NUMBER_ALREADY_REGISTERED"
    })
    void impedeEnvioECadastroParaDadosDuplicados(ErrorCode esperado) {
        if (esperado == ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED) {
            when(users.existsByEmail(EMAIL)).thenReturn(true);
        } else {
            when(users.existsByCardNumber(CARD)).thenReturn(true);
        }

        assertThatThrownBy(() -> service.sendVerificationCode(new SendVerificationCodeRequestDTO("Aluno", EMAIL, "123")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(esperado));
        verifyNoInteractions(codes, email);

        var verification = codigoValido();
        when(codes.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(verification));
        assertThatThrownBy(() -> service.register(cadastro(CODE)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(esperado));
        assertThat(verification.isUsed()).isFalse();
        verify(codes, never()).save(any(EmailVerificationCode.class));
        verify(users, never()).save(any(User.class));
        verifyNoInteractions(jwt);
    }

    @Test
    void cadastroConsomeCodigoECriaAlunoComSenhaCodificada() {
        var verification = codigoValido();
        when(codes.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(verification));
        when(encoder.encode(PASSWORD)).thenReturn("hash-de-teste");
        when(jwt.generateToken(CARD, "ALUNO")).thenReturn("token");

        assertThat(service.register(cadastro(CODE)))
                .isEqualTo(new LoginResponseDTO("token", "Aluno", CARD, Role.ALUNO));

        var captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        var user = captor.getValue();
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getCardNumber()).isEqualTo(CARD);
        assertThat(user.getPasswordHash()).isEqualTo("hash-de-teste");
        assertThat(user.getRole()).isEqualTo(Role.ALUNO);
        assertThat(verification.isUsed()).isTrue();
        verify(codes).save(verification);
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {
            "ERROR_VERIFICATION_CODE_NOT_FOUND", "ERROR_VERIFICATION_CODE_USED",
            "ERROR_VERIFICATION_CODE_EXPIRED", "ERROR_VERIFICATION_CODE_INVALID"
    })
    void rejeitaCodigoInutilizavelSemPersistirCadastro(ErrorCode esperado) {
        Optional<EmailVerificationCode> verification = switch (esperado) {
            case ERROR_VERIFICATION_CODE_NOT_FOUND -> Optional.empty();
            case ERROR_VERIFICATION_CODE_USED -> Optional.of(EmailVerificationCode.builder()
                    .code(CODE).used(true).expiresAt(LocalDateTime.now().plusHours(1)).build());
            case ERROR_VERIFICATION_CODE_EXPIRED -> Optional.of(EmailVerificationCode.builder()
                    .code(CODE).expiresAt(LocalDateTime.now().minusHours(1)).build());
            case ERROR_VERIFICATION_CODE_INVALID -> Optional.of(codigoValido());
            default -> throw new IllegalArgumentException("Cenário inesperado: " + esperado);
        };
        when(codes.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(verification);
        String codigoInformado = esperado == ErrorCode.ERROR_VERIFICATION_CODE_INVALID ? "999999" : CODE;

        assertThatThrownBy(() -> service.register(cadastro(codigoInformado)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(esperado));
        verify(codes, never()).save(any(EmailVerificationCode.class));
        verifyNoInteractions(users, encoder, jwt, email);
    }

    private EmailVerificationCode codigoValido() {
        return EmailVerificationCode.builder().email(EMAIL).code(CODE)
                .expiresAt(LocalDateTime.now().plusHours(1)).build();
    }

    private RegisterRequestDTO cadastro(String code) {
        return new RegisterRequestDTO("Aluno", EMAIL, "1-23", PASSWORD, code);
    }
}
