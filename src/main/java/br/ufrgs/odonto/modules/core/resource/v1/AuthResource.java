package br.ufrgs.odonto.modules.core.resource.v1;

import br.ufrgs.odonto.dto.auth.LoginRequestDTO;
import br.ufrgs.odonto.dto.auth.LoginResponseDTO;
import br.ufrgs.odonto.dto.auth.RegisterRequestDTO;
import br.ufrgs.odonto.dto.auth.SendVerificationCodeRequestDTO;
import br.ufrgs.odonto.modules.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login")
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login com número do cartão e senha")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify/send")
    @Operation(summary = "Envia código de verificação por e-mail para primeiro acesso")
    public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequestDTO request) {
        authService.sendVerificationCode(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @Operation(summary = "Primeiro acesso — valida código e cria conta de aluno")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }
}