package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.dto.user.CreateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateProfessorRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfessorServiceTest {

    private static final String EMAIL = "professor@example.invalid";

    private final UserRepository users = mock(UserRepository.class);
    private final UserMapper mapper = mock(UserMapper.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final ProfessorService service = new ProfessorService(users, mapper, encoder, new CardNumberNormalizer());

    @Test
    void criaProfessorComCartaoNormalizadoESenhaCodificada() {
        when(encoder.encode("senha-de-teste")).thenReturn("hash-de-teste");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new CreateProfessorRequestDTO("Professor", EMAIL, "1-23", "senha-de-teste"));

        var captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        var professor = captor.getValue();
        assertThat(professor.getName()).isEqualTo("Professor");
        assertThat(professor.getEmail()).isEqualTo(EMAIL);
        assertThat(professor.getCardNumber()).isEqualTo("00000123");
        assertThat(professor.getPasswordHash()).isEqualTo("hash-de-teste");
        assertThat(professor.getRole()).isEqualTo(Role.PROFESSOR);
        verify(mapper).toResponse(professor);
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {
            "ERROR_EMAIL_ALREADY_REGISTERED", "ERROR_CARD_NUMBER_ALREADY_REGISTERED"
    })
    void impedeCadastroDuplicado(ErrorCode esperado) {
        if (esperado == ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED) {
            when(users.existsByEmail(EMAIL)).thenReturn(true);
        } else {
            when(users.existsByCardNumber("00000123")).thenReturn(true);
        }

        assertThatThrownBy(() -> service.create(new CreateProfessorRequestDTO("Professor", EMAIL, "123", "senha-de-teste")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(esperado));
        verify(users, never()).save(any(User.class));
        verifyNoInteractions(encoder, mapper);
    }

    @Test
    void editaNomeEmailEEstadoDoProfessor() {
        var professor = User.builder().id(1L).role(Role.PROFESSOR).email(EMAIL).build();
        when(users.findById(1L)).thenReturn(Optional.of(professor));

        service.update(1L, new UpdateProfessorRequestDTO("Novo nome", "novo@example.invalid", false));

        assertThat(professor.getName()).isEqualTo("Novo nome");
        assertThat(professor.getEmail()).isEqualTo("novo@example.invalid");
        assertThat(professor.isActive()).isFalse();
        verify(users).save(professor);
    }

    @Test
    void rejeitaEmailDuplicadoSemAlterarProfessor() {
        var professor = User.builder().id(1L).name("Professor").role(Role.PROFESSOR).email(EMAIL).build();
        when(users.findById(1L)).thenReturn(Optional.of(professor));
        when(users.existsByEmail("ocupado@example.invalid")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateProfessorRequestDTO("Novo nome", "ocupado@example.invalid", false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED));
        assertThat(professor.getEmail()).isEqualTo(EMAIL);
        assertThat(professor.getName()).isEqualTo("Professor");
        assertThat(professor.isActive()).isTrue();
        verify(users, never()).save(any(User.class));
    }

    @Test
    void rejeitaAlunoEmOperacaoDeProfessor() {
        var aluno = User.builder().id(1L).role(Role.ALUNO).build();
        when(users.findById(1L)).thenReturn(Optional.of(aluno));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_USER_NOT_PROFESSOR));
        verify(users, never()).save(any(User.class));
    }

    @Test
    void professorInexistenteGeraErroDeUsuarioNaoEncontrado() {
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOfSatisfying(ResourceNotFoundException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ERROR_USER_NOT_FOUND));
        verifyNoInteractions(mapper);
    }

    @Test
    void exclusaoInativaProfessorPreservandoRegistro() {
        var professor = User.builder().id(1L).role(Role.PROFESSOR).build();
        when(users.findById(1L)).thenReturn(Optional.of(professor));

        service.delete(1L);

        assertThat(professor.isActive()).isFalse();
        verify(users).save(professor);
        verify(users, never()).delete(any(User.class));
    }
}
