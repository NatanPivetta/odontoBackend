package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.config.auth.JwtService;
import br.ufrgs.odonto.dto.auth.LoginRequestDTO;
import br.ufrgs.odonto.dto.auth.LoginResponseDTO;
import br.ufrgs.odonto.modules.core.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.cardNumber(),
                        request.password()
                )
        );

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user.getCardNumber(), user.getRole().name());

        return new LoginResponseDTO(
                token,
                user.getName(),
                user.getCardNumber(),
                user.getRole()
        );
    }
}