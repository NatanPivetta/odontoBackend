package br.ufrgs.odonto.config.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String TEST_KEY = "odonto-jwt-unit-test-key-0123456789-abcdefghijklmnopqrstuvwxyz-0123456789";

    private final JwtService service = new JwtService(new JwtProperties(TEST_KEY, 60_000));

    @Test
    void tokenValidoPreservaCartaoEPapel() {
        String token = service.generateToken("00000123", "ALUNO");

        assertThat(service.isTokenValid(token)).isTrue();
        assertThat(service.extractCardNumber(token)).isEqualTo("00000123");
        assertThat(service.extractClaims(token).get("role", String.class)).isEqualTo("ALUNO");
    }

    @Test
    void rejeitaTokenExpirado() {
        var emissor = new JwtService(new JwtProperties(TEST_KEY, -60_000));
        assertThat(service.isTokenValid(emissor.generateToken("00000123", "ALUNO"))).isFalse();
    }

    @Test
    void rejeitaTokenMalformadoEVazio() {
        assertThat(service.isTokenValid("token-sem-formato-jwt")).isFalse();
        assertThat(service.isTokenValid("")).isFalse();
    }
}
