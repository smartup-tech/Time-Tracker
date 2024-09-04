package ru.smartup.timetracker.email.template.impl;

import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.EmailConstant;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;

import java.util.Map;

public class PasswordUpdateTemplate extends BaseEmailTemplate {
    private static final String PASSWORD_UPDATED_TEMPLATE = "passwordUpdated.html";

    @Override
    public EmailXmlTemplate getTemplate(final Notice notice) {
        return getPasswordUpdatedTemplate();
    }

    private EmailXmlTemplate getPasswordUpdatedTemplate() {
        return new EmailXmlTemplate(
                PASSWORD_UPDATED_TEMPLATE,
                EmailConstant.SubjectName.PASSWORD_RECOVERY_SUBJECT,
                Map.of()
        );
    }
}
