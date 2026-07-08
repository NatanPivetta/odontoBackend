package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.dto.user.CreateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateProfessorRequestDTO;
import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.enums.Role;
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
public class ProfessorService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CardNumberNormalizer cardNumberNormalizer;

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Boolean active, Pageable pageable) {
        if (active != null) {
            return userRepository.findByRoleAndActive(Role.PROFESSOR, active, pageable)
                    .map(userMapper::toResponse);
        }
        return userRepository.findByRole(Role.PROFESSOR, pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return userMapper.toResponse(findProfessorById(id));
    }

    @Transactional
    public UserResponseDTO create(CreateProfessorRequestDTO request) {
        String cardNumber = cardNumberNormalizer.normalize(request.cardNumber());
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED);
        }
        if (userRepository.existsByCardNumber(cardNumber)) {
            throw new BusinessException(ErrorCode.ERROR_CARD_NUMBER_ALREADY_REGISTERED);
        }

        User professor = User.builder()
                .name(request.name())
                .email(request.email())
                .cardNumber(cardNumber)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.PROFESSOR)
                .build();

        return userMapper.toResponse(userRepository.save(professor));
    }

    @Transactional
    public UserResponseDTO update(Long id, UpdateProfessorRequestDTO request) {
        User professor = findProfessorById(id);

        if (request.email() != null && !request.email().equals(professor.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException(ErrorCode.ERROR_EMAIL_ALREADY_REGISTERED);
            }
            professor.setEmail(request.email());
        }
        if (request.name() != null) {
            professor.setName(request.name());
        }
        if (request.active() != null) {
            professor.setActive(request.active());
        }

        return userMapper.toResponse(userRepository.save(professor));
    }

    @Transactional
    public void delete(Long id) {
        User professor = findProfessorById(id);
        professor.setActive(false);
        userRepository.save(professor);
    }

    private User findProfessorById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND, id));
        if (user.getRole() != Role.PROFESSOR) {
            throw new BusinessException(ErrorCode.ERROR_USER_NOT_PROFESSOR);
        }
        return user;
    }
}
