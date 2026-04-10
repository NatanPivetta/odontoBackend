package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.dto.user.CreateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UpdateUserRequestDTO;
import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.exception.BusinessException;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    public UserResponseDTO findById(UUID id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Transactional
    public UserResponseDTO create(CreateUserRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado: " + request.email());
        }
        if (userRepository.existsByCardNumber(request.cardNumber())) {
            throw new BusinessException("Número de cartão já cadastrado: " + request.cardNumber());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .cardNumber(request.cardNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO update(UUID id, UpdateUserRequestDTO request) {
        User user = findUserById(id);

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException("E-mail já cadastrado: " + request.email());
            }
            user.setEmail(request.email());
        }
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }
}
