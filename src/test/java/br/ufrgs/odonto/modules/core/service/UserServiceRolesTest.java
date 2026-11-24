package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.dto.user.CreateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateUserRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.enums.RoleName;
import br.ufrgs.odonto.enums.PermissionName;
import br.ufrgs.odonto.modules.core.entity.*;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.core.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceRolesTest {
    private final UserRepository users = mock(UserRepository.class);
    private final SecurityRoleRepository roles = mock(SecurityRoleRepository.class);
    private final UserMapper mapper = mock(UserMapper.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UserService service = new UserService(users, roles, mapper, encoder, new CardNumberNormalizer());

    private SecurityRole studentRole() {
        return SecurityRole.builder().id(3L).name(RoleName.ALUNO)
                .permissions(Set.of(SecurityPermission.builder().id(1L)
                        .name(PermissionName.ACTIVITY_VIEW_OWN).build())).build();
    }

    @Test
    void newStudentReceivesPermissionToListOwnActivities() {
        when(roles.findByName(RoleName.ALUNO)).thenReturn(Optional.of(studentRole()));
        when(encoder.encode("password123")).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getRole()).isEqualTo(Role.ALUNO);
            assertThat(user.getAuthorities()).extracting(authority -> authority.getAuthority())
                    .contains("ROLE_ALUNO", "ACTIVITY_VIEW_OWN");
            return user;
        });
        service.create(new CreateUserRequestDTO("Aluno", "aluno@example.com", "7", "password123", Role.ALUNO));
        verify(users).save(any(User.class));
    }

    @Test
    void changingToStudentReplacesProfessorAndPreservesAdditionalProfiles() {
        User user = User.builder().id(7L).role(Role.PROFESSOR).build();
        SecurityRole monitor = SecurityRole.builder().id(4L).name(RoleName.MONITOR).build();
        user.getRoles().add(SecurityRole.builder().id(2L).name(RoleName.PROFESSOR).build());
        user.getRoles().add(monitor);
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(roles.findByName(RoleName.ALUNO)).thenReturn(Optional.of(studentRole()));
        when(users.save(user)).thenReturn(user);
        service.update(7L, new UpdateUserRequestDTO(null, null, Role.ALUNO, null));
        assertThat(user.getRoles()).extracting(SecurityRole::getName)
                .containsExactlyInAnyOrder(RoleName.ALUNO, RoleName.MONITOR);
        assertThat(user.getAuthorities()).extracting(authority -> authority.getAuthority())
                .contains("ACTIVITY_VIEW_OWN").doesNotContain("ROLE_PROFESSOR");
    }
}