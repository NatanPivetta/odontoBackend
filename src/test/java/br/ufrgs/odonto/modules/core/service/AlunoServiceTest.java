package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.enums.Role;
import br.ufrgs.odonto.modules.core.entity.User;
import br.ufrgs.odonto.modules.core.mapper.UserMapper;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AlunoServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final UserMapper mapper = mock(UserMapper.class);
    private final AlunoService service = new AlunoService(users, mapper);
    private final PageRequest pagina = PageRequest.of(0, 10);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void consultaAlunosPeloEstadoSolicitadoEPreservaPaginacao(boolean active) {
        var aluno = User.builder().id(1L).role(Role.ALUNO).active(active).build();
        var response = mock(UserResponseDTO.class);
        when(users.findByRoleAndActive(Role.ALUNO, active, pagina))
                .thenReturn(new PageImpl<>(List.of(aluno), pagina, 15));
        when(mapper.toResponse(aluno)).thenReturn(response);

        var resultado = service.findAll(active, pagina);

        assertThat(resultado.getContent()).containsExactly(response);
        assertThat(resultado.getTotalElements()).isEqualTo(15);
        assertThat(resultado.getPageable()).isEqualTo(pagina);
    }

    @Test
    void estadoOmitidoConsultaTodosOsAlunos() {
        when(users.findByRole(Role.ALUNO, pagina)).thenReturn(Page.empty(pagina));

        assertThat(service.findAll(null, pagina)).isEmpty();
        verify(users).findByRole(Role.ALUNO, pagina);
        verifyNoInteractions(mapper);
    }
}
