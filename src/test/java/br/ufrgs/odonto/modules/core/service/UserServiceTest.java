package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.dto.user.CreateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateUserRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.enums.RoleName;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.core.repository.SecurityRoleRepository;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private static final String EMAIL = "usuario@example.invalid";

    private final UserRepository users = mock(UserRepository.class);
    private final SecurityRoleRepository roles = mock(SecurityRoleRepository.class);
    private final UserMapper mapper = mock(UserMapper.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UserService service = new UserService(users, roles, mapper, encoder, new CardNumberNormalizer());

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {
            "ERROR_EMAIL_ALREADY_REGISTERED", "ERROR_CARD_NUMBER_ALREADY_REGISTERED"
    })
    void rejeitaUsuarioDuplicadoSemPersistir(ErrorCode esperado) {
        if (esperado == ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED) {
            when(users.existsByEmail(EMAIL)).thenReturn(true);
        } else {
            when(users.existsByCardNumber("00000123")).thenReturn(true);
        }

        assertThatThrownBy(() -> service.create(cadastro()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(esperado));
        verify(users, never()).save(any(User.class));
        verifyNoInteractions(roles, encoder, mapper);
    }

    @Test
    void cadastroSemPapelConfiguradoFalhaSemPersistir() {
        when(roles.findByName(RoleName.ALUNO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(cadastro())).isInstanceOf(IllegalStateException.class);
        verify(users, never()).save(any(User.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void rejeitaEmailJaUsadoSemAlterarDados() {
        var user = User.builder().id(1L).name("Aluno").email(EMAIL).role(Role.ALUNO).build();
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(users.existsByEmail("ocupado@example.invalid")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateUserRequestDTO("Novo nome", "ocupado@example.invalid", null, false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED));
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getName()).isEqualTo("Aluno");
        assertThat(user.isActive()).isTrue();
        verify(users, never()).save(any(User.class));
    }

    @Test
    void editaDadosSemSubstituirPapelQuandoOmitido() {
        var user = User.builder().id(1L).role(Role.ALUNO).email(EMAIL).active(false).build();
        when(users.findById(1L)).thenReturn(Optional.of(user));

        service.update(1L, new UpdateUserRequestDTO("Novo nome", "novo@example.invalid", null, true));

        assertThat(user.getName()).isEqualTo("Novo nome");
        assertThat(user.getEmail()).isEqualTo("novo@example.invalid");
        assertThat(user.getRole()).isEqualTo(Role.ALUNO);
        assertThat(user.isActive()).isTrue();
        verify(users).save(user);
        verifyNoInteractions(roles);
    }

    @Test
    void exclusaoInativaUsuarioPreservandoRegistro() {
        var user = User.builder().id(1L).role(Role.ALUNO).build();
        when(users.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        assertThat(user.isActive()).isFalse();
        verify(users).save(user);
        verify(users, never()).delete(any(User.class));
    }

    @Test
    void consultaUsuarioInexistenteGeraErro() {
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOfSatisfying(ResourceNotFoundException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_USER_NOT_FOUND));
        verifyNoInteractions(mapper);
    }

    private CreateUserRequestDTO cadastro() {
        return new CreateUserRequestDTO("Aluno", EMAIL, "123", "senha-de-teste", Role.ALUNO);
    }
}
