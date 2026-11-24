package br.ufrgs.odonto.security;

import br.ufrgs.odonto.enums.PermissionName;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.enums.RoleName;
import br.ufrgs.odonto.modules.core.entity.SecurityPermission;
import br.ufrgs.odonto.modules.core.entity.SecurityRole;
import br.ufrgs.odonto.modules.core.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UserAuthoritiesTest {

    @Test
    void getAuthoritiesIncluiRoleLegadaRolePersistidaEPermissions() {
        SecurityPermission classView = SecurityPermission.builder()
                .id(1L).name(PermissionName.CLASS_VIEW)
                .build();
        SecurityPermission activityCreate = SecurityPermission.builder()
                .id(2L).name(PermissionName.ACTIVITY_CREATE)
                .build();
        SecurityRole alunoRole = SecurityRole.builder()
                .name(RoleName.ALUNO)
                .permissions(Set.of(classView, activityCreate))
                .build();
        User user = User.builder()
                .role(Role.ALUNO)
                .roles(Set.of(alunoRole))
                .build();

        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities)
                .contains("ROLE_ALUNO", "CLASS_VIEW", "ACTIVITY_CREATE");
    }
}
