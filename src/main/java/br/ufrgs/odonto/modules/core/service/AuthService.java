package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.config.auth.JwtService;
import br.ufrgs.odonto.dto.auth.LoginRequestDTO;
import br.ufrgs.odonto.dto.auth.LoginResponseDTO;
import br.ufrgs.odonto.dto.auth.RegisterRequestDTO;
import br.ufrgs.odonto.dto.auth.SendVerificationCodeRequestDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.modules.core.entity.EmailVerificationCode;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.repository.EmailVerificationCodeRepository;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.cardNumber(), request.password())
        );
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user.getCardNumber(), user.getRole().name());
        return new LoginResponseDTO(token, user.getName(), user.getCardNumber(), user.getRole());
    }

    @Transactional
    public void sendVerificationCode(SendVerificationCodeRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED);
        }
        if (userRepository.existsByCardNumber(request.cardNumber())) {
            throw new BusinessException(ErrorCode.ERROR_CARD_NUMBER_ALREADY_REGISTERED);
        }

        verificationCodeRepository.deleteByEmail(request.email());

        String code = String.format("%06d", new Random().nextInt(1_000_000));

        verificationCodeRepository.save(EmailVerificationCode.builder()
                .email(request.email())
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build());

        emailService.sendVerificationCode(request.email(), code);
    }

    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO request) {
        EmailVerificationCode verification = verificationCodeRepository
                .findTopByEmailOrderByCreatedAtDesc(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERROR_VERIFICATION_CODE_NOT_FOUND));

        if (verification.isUsed()) {
            throw new BusinessException(ErrorCode.ERROR_VERIFICATION_CODE_USED);
        }
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ERROR_VERIFICATION_CODE_EXPIRED);
        }
        if (!verification.getCode().equals(request.code())) {
            throw new BusinessException(ErrorCode.ERROR_VERIFICATION_CODE_INVALID);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED);
        }
        if (userRepository.existsByCardNumber(request.cardNumber())) {
            throw new BusinessException(ErrorCode.ERROR_CARD_NUMBER_ALREADY_REGISTERED);
        }

        verification.setUsed(true);
        verificationCodeRepository.save(verification);

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .cardNumber(request.cardNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.ALUNO)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getCardNumber(), user.getRole().name());
        return new LoginResponseDTO(token, user.getName(), user.getCardNumber(), user.getRole());
    }
}
