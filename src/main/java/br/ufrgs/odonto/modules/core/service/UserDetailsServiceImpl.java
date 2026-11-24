package br.ufrgs.odonto.modules.core.service;

import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.modules.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final CardNumberNormalizer cardNumberNormalizer;

    @Override
    public UserDetails loadUserByUsername(String cardNumber) throws UsernameNotFoundException {
        String normalizedCardNumber = cardNumberNormalizer.normalize(cardNumber);
        return userRepository.findByCardNumber(normalizedCardNumber)
                .orElseThrow(() -> new UsernameNotFoundException(
                        ErrorCode.ERROR_AUTH_INVALID_CREDENTIALS.name()));
    }
}
