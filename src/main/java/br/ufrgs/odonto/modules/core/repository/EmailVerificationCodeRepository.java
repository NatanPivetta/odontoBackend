package br.ufrgs.odonto.modules.core.repository;

import br.ufrgs.odonto.modules.core.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}
