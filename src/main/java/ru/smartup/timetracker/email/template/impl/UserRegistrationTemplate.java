package ru.smartup.timetracker.email.template.impl;

import lombok.AllArgsConstructor;
import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.EmailConstant;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.pojo.notice.NoticePersonalToken;

import java.util.Map;

@AllArgsConstructor
public class UserRegistrationTemplate extends BaseEmailTemplate {
    private static final String USER_REGISTRATION_TEMPLATE = "userRegistration.html";
    private final String registrationLink;

    @Override
    public EmailXmlTemplate getTemplate(final Notice notice) {
        return getUserRegistrationTemplateGenerator((NoticePersonalToken) notice.getData());
    }

    private EmailXmlTemplate getUserRegistrationTemplateGenerator(final NoticePersonalToken data) {
        return new EmailXmlTemplate(
                USER_REGISTRATION_TEMPLATE,
                EmailConstant.SubjectName.USER_REGISTRATION_SUBJECT,
                Map.of(
                        EmailConstant.PropertyName.NAME_PROPERTY, data.getUsername(),
                        EmailConstant.PropertyName.LINK_PROPERTY, registrationLink + data.getToken(),
                        EmailConstant.PropertyName.TTL_PROPERTY, data.getTtlInHours() == 1 ? data.getTtlInHours() + EmailConstant.HOUR : data.getTtlInHours() + EmailConstant.HOURS
                )
        );
    };
}
