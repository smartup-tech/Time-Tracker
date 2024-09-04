package ru.smartup.timetracker.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.impl.*;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;

import java.util.Map;

@Configuration
public class EmailTemplateConfig {

    @Bean
    public Map<NoticeTypeEnum, BaseEmailTemplate> emailTemplates(final @Value("${token.recovery.link}") String passwordRecoveryLink,
                                                                 final @Value("${token.registration.link}") String passwordRegistrationLink) {
        return Map.of(
                NoticeTypeEnum.PASSWORD_RECOVERY, new PasswordRecoveryTemplate(passwordRecoveryLink),
                NoticeTypeEnum.PASSWORD_UPDATE, new PasswordUpdateTemplate(),
                NoticeTypeEnum.REGISTER_NEW_USER, new UserRegistrationTemplate(passwordRegistrationLink),
                NoticeTypeEnum.PASSWORD_RESET, new PasswordResetTemplate(),
                NoticeTypeEnum.FREEZE_PREPARE, new ScheduleFreezeTemplate(),
                NoticeTypeEnum.FREEZE_CANCEL, new CancelScheduleFreezeTemplate(),
                NoticeTypeEnum.UN_FREEZE, new UnfreezeHoursTemplate(),
                NoticeTypeEnum.FREEZE_SUCCESS, new SuccessFreezeTemplate()
        );
    }

}
