package ru.smartup.timetracker.email.template.impl;

import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.EmailConstant;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.pojo.notice.NoticePersonal;

import java.util.Map;

public class PasswordResetTemplate extends BaseEmailTemplate {
    private static final String PASSWORD_RESET_TEMPLATE = "passwordReset.html";
    @Override
    public EmailXmlTemplate getTemplate(final Notice notice) {
        return getPasswordResetTemplate((NoticePersonal) notice.getData());
    }

    private EmailXmlTemplate getPasswordResetTemplate(final NoticePersonal data) {
        return new EmailXmlTemplate(
                PASSWORD_RESET_TEMPLATE,
                EmailConstant.SubjectName.PASSWORD_RESET_SUBJECT,
                Map.of(
                        EmailConstant.PropertyName.NAME_PROPERTY, data.getUsername()
                )
        );
    }
}
