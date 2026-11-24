package br.ufrgs.odonto.modules.core.service;

import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final EmailService service = new EmailService(sender);
    private final MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));

    @BeforeEach
    void preparaMensagem() {
        ReflectionTestUtils.setField(service, "fromEmail", "clinodonto@example.invalid");
        when(sender.createMimeMessage()).thenReturn(message);
    }

    @Test
    void enviaMensagemComRemetenteDestinatarioECodigo() throws Exception {
        service.sendVerificationCode("aluno@example.invalid", "012345");

        verify(sender).send(message);
        assertThat(message.getFrom()).containsExactly(new InternetAddress("clinodonto@example.invalid"));
        assertThat(message.getAllRecipients()).containsExactly(new InternetAddress("aluno@example.invalid"));
        var root = (Multipart) message.getContent();
        var content = (Multipart) root.getBodyPart(0).getContent();
        assertThat(content.getBodyPart(0).getContent().toString()).contains("012345");
    }

    @Test
    void propagaFalhaDoTransporte() {
        var falha = new MailSendException("SMTP de teste indisponível");
        doThrow(falha).when(sender).send(message);

        assertThatThrownBy(() -> service.sendVerificationCode("aluno@example.invalid", "012345"))
                .isSameAs(falha);
    }

    @Test
    void encapsulaDestinatarioInvalidoSemEnviarMensagem() {
        assertThatThrownBy(() -> service.sendVerificationCode("aluno@", "012345"))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(jakarta.mail.MessagingException.class);
        verify(sender, never()).send(any(MimeMessage.class));
    }
}
