package br.ufrgs.odonto.modules.core.repository;

import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.modules.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCardNumber(String cardNumber);

    Optional<User> findByEmail(String email);

    boolean existsByCardNumber(String cardNumber);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByRoleAndActive(Role role, boolean active, Pageable pageable);
}