package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.enums.RoleName;
import br.ufrgs.odonto.modules.core.repository.SecurityRoleRepository;
import br.ufrgs.odonto.dto.user.CreateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SecurityRoleRepository securityRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CardNumberNormalizer cardNumberNormalizer;

    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    public UserResponseDTO findById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Transactional
    public UserResponseDTO create(CreateUserRequestDTO request) {
        String cardNumber = cardNumberNormalizer.normalize(request.cardNumber());
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED);
        }
        if (userRepository.existsByCardNumber(cardNumber)) {
            throw new BusinessException(ErrorCode.ERROR_CARD_NUMBER_ALREADY_REGISTERED);
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .cardNumber(cardNumber)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();
        user.getRoles().add(securityRoleRepository.findByName(RoleName.valueOf(request.role().name()))
                .orElseThrow(() -> new IllegalStateException("Security role not configured: " + request.role())));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO update(Long id, UpdateUserRequestDTO request) {
        User user = findUserById(id);

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED);
            }
            user.setEmail(request.email());
        }
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.role() != null) {
            var securityRole = securityRoleRepository.findByName(RoleName.valueOf(request.role().name()))
                    .orElseThrow(() -> new IllegalStateException("Security role not configured: " + request.role()));
            // Replace legacy base roles while preserving additional RBAC profiles.
            user.getRoles().removeIf(role -> role.getName() == RoleName.ALUNO
                    || role.getName() == RoleName.PROFESSOR);
            user.getRoles().add(securityRole);
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND, id));
    }
}
