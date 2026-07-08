package br.ufrgs.odonto.config;

import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_CARD_NUMBER = "00000001";
    private static final String DEFAULT_PASSWORD    = "Admin@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByCardNumber(DEFAULT_CARD_NUMBER)) {
            return;
        }

        User admin = User.builder()
                .name("Administrador")
                .email("admin@odonto.ufrgs.br")
                .cardNumber(DEFAULT_CARD_NUMBER)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(Role.PROFESSOR)
                .build();

        userRepository.save(admin);

        log.warn("==========================================================");
        log.warn("  Usuário padrão criado — altere a senha após o primeiro login");
        log.warn("  Cartão : {}", DEFAULT_CARD_NUMBER);
        log.warn("  Senha  : {}", DEFAULT_PASSWORD);
        log.warn("==========================================================");
    }
}
