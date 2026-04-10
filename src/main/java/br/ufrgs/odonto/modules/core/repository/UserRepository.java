package br.ufrgs.odonto.modules.core.repository;

import br.ufrgs.odonto.modules.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCardNumber(String cardNumber);

    Optional<User> findByEmail(String email);

    boolean existsByCardNumber(String cardNumber);

    boolean existsByEmail(String email);
}