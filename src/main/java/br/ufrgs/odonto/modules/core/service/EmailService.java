package br.ufrgs.odonto.modules.core.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "ClinOdonto UFRGS");
            helper.setTo(toEmail);
            helper.setSubject("Seu código de verificação — ClinOdonto");
            helper.setText(buildEmailBody(code), true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Erro ao enviar e-mail de verificação", e);
        }
    }

    private String buildEmailBody(String code) {
        return """
                <div style="font-family:sans-serif;max-width:480px;margin:0 auto;padding:32px;">
                  <div style="margin-bottom:24px;">
                    <span style="background:#0d9488;color:#fff;font-weight:700;padding:6px 12px;border-radius:6px;font-size:14px;">
                      ClinOdonto UFRGS
                    </span>
                  </div>
                  <h2 style="color:#111827;font-size:22px;margin-bottom:8px;">Código de verificação</h2>
                  <p style="color:#374151;margin-bottom:24px;">
                    Use o código abaixo para concluir seu primeiro acesso ao sistema.
                  </p>
                  <div style="background:#f0fdfa;border:2px solid #99f6e4;border-radius:12px;padding:24px;text-align:center;margin-bottom:24px;">
                    <span style="font-size:40px;font-weight:700;letter-spacing:12px;color:#0d9488;font-family:monospace;">
                      %s
                    </span>
                  </div>
                  <p style="color:#6b7280;font-size:13px;line-height:1.6;">
                    O código é válido por <strong>15 minutos</strong>.<br>
                    Caso não tenha solicitado este cadastro, ignore este e-mail.
                  </p>
                </div>
                """.formatted(code);
    }
}
