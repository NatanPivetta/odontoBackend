package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Boolean active, Pageable pageable) {
        if (active != null) {
            return userRepository.findByRoleAndActive(Role.ALUNO, active, pageable)
                    .map(userMapper::toResponse);
        }
        return userRepository.findByRole(Role.ALUNO, pageable)
                .map(userMapper::toResponse);
    }
}
