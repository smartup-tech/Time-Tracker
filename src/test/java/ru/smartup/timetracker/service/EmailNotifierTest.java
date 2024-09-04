package ru.smartup.timetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.smartup.timetracker.email.template.EmailTemplateStrategy;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.service.notification.notifierImpl.EmailNotifier;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmailNotifierTest {
    private static final String FROM = "no-reply-time-tracker@smartup.ru";
    private static final String SUBJECT = "subject";
    private static final String TEMPLATE = "template";
    private static final String PROPERTY = "property";
    private static final String VALUE = "value";
    private static final String PASSWORD = "password";

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final SpringTemplateEngine templateEngine = spy(SpringTemplateEngine.class);
    private final EmailTemplateStrategy emailTemplateStrategy = mock(EmailTemplateStrategy.class);

    private EmailNotifier emailNotifier;

    @BeforeEach
    public void setUp() {
        emailNotifier = new EmailNotifier(FROM, mailSender, templateEngine, emailTemplateStrategy);
    }

    @Test
    public void send_shouldDeliverMessage() {
        MimeMessage message = createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        User user = createUserObj();

        Notice notice = mock(Notice.class);

        when(emailTemplateStrategy.getTemplate(any(Notice.class))).thenReturn(createEmailTemplate());

        emailNotifier.send(List.of(user), notice);

        verify(mailSender, timeout(5_000)).createMimeMessage();
        verify(mailSender, timeout(5_000)).send(message);
    }

    @Test
    public void send_shouldHandleException() {
        MimeMessage message = createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        doThrow(MailSendException.class).when(mailSender).send(message);

        Notice notice = mock(Notice.class);

        when(emailTemplateStrategy.getTemplate(any(Notice.class))).thenReturn(createEmailTemplate());

        emailNotifier.send(List.of(createUserObj()), notice);

        verify(mailSender, timeout(5_000)).createMimeMessage();
        verify(mailSender, timeout(5_000)).send(message);
    }

    private MimeMessage createMimeMessage() {
        Session session = Session.getInstance(new Properties(), createAuthenticator());
        return new MimeMessage(session);
    }

    private Authenticator createAuthenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(this.getDefaultUserName(), PASSWORD);
            }
        };
    }

    private User createUserObj() {
        User user = new User();
        user.setId(1);
        user.setEmail("qwerty12345@smartup.ru");
        user.setFirstName("fisrtname");
        user.setPositionId(1);
        return user;
    }

    private EmailXmlTemplate createEmailTemplate() {
        return new EmailXmlTemplate(TEMPLATE, SUBJECT, Map.of(PROPERTY, VALUE));
    }
}