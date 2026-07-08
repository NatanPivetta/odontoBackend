package br.ufrgs.odonto.modules.core.service;

import org.springframework.stereotype.Component;

@Component
public class CardNumberNormalizer {

    public static final int CARD_NUMBER_LENGTH = 8;

    public String normalize(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }

        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() > CARD_NUMBER_LENGTH) {
            return digits;
        }

        return "0".repeat(CARD_NUMBER_LENGTH - digits.length()) + digits;
    }
}
