package br.ufrgs.odonto.modules.core.repository;

import br.ufrgs.odonto.enums.PermissionName;
import br.ufrgs.odonto.modules.core.entity.SecurityPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityPermissionRepository extends JpaRepository<SecurityPermission, Long> {

    Optional<SecurityPermission> findByName(PermissionName name);
}
