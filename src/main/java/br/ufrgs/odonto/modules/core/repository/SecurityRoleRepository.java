package br.ufrgs.odonto.modules.core.repository;

import br.ufrgs.odonto.enums.RoleName;
import br.ufrgs.odonto.modules.core.entity.SecurityRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityRoleRepository extends JpaRepository<SecurityRole, Long> {

    Optional<SecurityRole> findByName(RoleName name);
}
