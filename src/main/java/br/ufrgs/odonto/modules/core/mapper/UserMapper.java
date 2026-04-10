package br.ufrgs.odonto.modules.core.mapper;

import br.ufrgs.odonto.dto.user.UserResponseDTO;
import br.ufrgs.odonto.modules.core.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserResponseDTO toResponse(User user);
}