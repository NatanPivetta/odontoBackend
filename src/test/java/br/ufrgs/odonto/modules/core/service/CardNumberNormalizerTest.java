package br.ufrgs.odonto.modules.core.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberNormalizerTest {

    private final CardNumberNormalizer normalizer = new CardNumberNormalizer();

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "NULL, NULL",
            "'', 00000000",
            "12.345-6, 00123456",
            "00001234, 00001234",
            "12345678, 12345678",
            "9012345678, 12345678"
    })
    void normalizaParaOFormatoDeOitoDigitos(String entrada, String esperado) {
        assertThat(normalizer.normalize(entrada)).isEqualTo(esperado);
    }
}
