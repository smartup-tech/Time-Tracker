package ru.smartup.timetracker.core.notification;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.smartup.timetracker.email.template.EmailTemplateStrategy;
import ru.smartup.timetracker.service.notification.NoticeService;
import ru.smartup.timetracker.service.notification.notifier.Notifier;
import ru.smartup.timetracker.service.notification.notifierImpl.DbNotifier;
import ru.smartup.timetracker.service.notification.notifierImpl.EmailNotifier;

import java.util.HashMap;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class NotifiersConfig {
    private final EmailNotifierProperties emailConfig;
    private final EmailTemplateStrategy emailTemplateStrategy;

    @Bean
    public Map<String, Notifier> notifiers(final EmailNotifier emailNotifier,
                                           final DbNotifier dbNotifier) {
        final Map<String, Notifier> notifiers = new HashMap<>();

        notifiers.put(NotifierNames.EMAIL_NOTIFIER, emailNotifier);
        notifiers.put(NotifierNames.DATABASE_NOTIFIER, dbNotifier);

        return notifiers;
    }

    @Bean
    public EmailNotifier emailNotifier(final JavaMailSender javaMailSender,
                                       final SpringTemplateEngine templateEngine) {
        return new EmailNotifier(emailConfig.getUsername(), javaMailSender, templateEngine, emailTemplateStrategy);
    }

    @Bean
    public DbNotifier dbNotifier(final NoticeService noticeService) {
        return new DbNotifier(noticeService);
    }
}
