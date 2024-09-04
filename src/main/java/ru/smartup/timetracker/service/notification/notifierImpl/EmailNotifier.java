package ru.smartup.timetracker.service.notification.notifierImpl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.smartup.timetracker.email.template.EmailTemplateStrategy;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.service.notification.notifier.Notifier;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class EmailNotifier implements Notifier {
    private final String from;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailTemplateStrategy emailTemplate;

    @Override
    public void send(final List<User> recipients, final Notice notice) {
        final List<String> recipientsEmail = recipients
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        EmailXmlTemplate template = emailTemplate.getTemplate(notice);

        if (template != null) {
            sendSync(recipientsEmail, template);
        }
    }

    private void sendSync(final List<String> to, final EmailXmlTemplate template) {
        try {
            String htmlBody = generateHtmlBody(template);
            MimeMessage message = generateMessage(to, htmlBody, template.getTemplateSubject());
            log.info("SEND");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Email send fail : {}", e.getMessage());
        }
    }

    private String generateHtmlBody(final EmailXmlTemplate template) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(template.getTemplateProperties());
        return templateEngine.process(template.getTemplateName(), thymeleafContext);
    }

    private MimeMessage generateMessage(final List<String> to, final String htmlBody, final String subject) {
        MimeMessage message = mailSender.createMimeMessage();

        setMessageTemplateAndRecipients(message, to, htmlBody, subject);

        return message;
    }

    private void setMessageTemplateAndRecipients(final MimeMessage message, final List<String> to, final String htmlBody, final String subject) {
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
        } catch (MessagingException e) {
            log.error("Can not send message to email addresses = {}", to, e);
        }
    }

}
