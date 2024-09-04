package ru.smartup.timetracker.email.template.impl;

import lombok.AllArgsConstructor;
import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.EmailConstant;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.pojo.notice.NoticePersonalToken;

import java.util.Map;

@AllArgsConstructor
public class PasswordRecoveryTemplate extends BaseEmailTemplate {

    private static final String PASSWORD_RECOVERY_TEMPLATE = "passwordRecovery.html";
    private final String passwordRecoveryLink;

    @Override
    public EmailXmlTemplate getTemplate(final Notice notice) {
        return getPasswordRecoveryTemplate((NoticePersonalToken) notice.getData());
    }

    private EmailXmlTemplate getPasswordRecoveryTemplate(final NoticePersonalToken notice) {
        final Map<String, Object> templateProperties = Map.of(
                EmailConstant.PropertyName.NAME_PROPERTY, notice.getUsername(),
                EmailConstant.PropertyName.LINK_PROPERTY, passwordRecoveryLink + notice.getToken(),
                EmailConstant.PropertyName.TTL_PROPERTY, notice.getTtlInHours() == 1 ? notice.getTtlInHours() + EmailConstant.HOUR : notice.getTtlInHours() + EmailConstant.HOURS
        );

        return new EmailXmlTemplate(PASSWORD_RECOVERY_TEMPLATE, EmailConstant.SubjectName.PASSWORD_RECOVERY_SUBJECT, templateProperties);
    }
}
