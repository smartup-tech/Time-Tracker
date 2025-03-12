package ru.smartup.timetracker.email.template.impl;

import lombok.AllArgsConstructor;
import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.EmailConstant;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.pojo.notice.NoticePersonalToken;

import java.util.Map;

@AllArgsConstructor
public class EmployeeRegistrationTemplate extends BaseEmailTemplate {
    private static final String EMPLOYEE_REGISTRATION_TEMPLATE = "userRegistration.html";
    private final String registrationLink;

    @Override
    public EmailXmlTemplate getTemplate(final Notice notice) {
        return getEmployeeRegistrationTemplateGenerator((NoticePersonalToken) notice.getData());
    }

    private EmailXmlTemplate getEmployeeRegistrationTemplateGenerator(final NoticePersonalToken data) {
        return new EmailXmlTemplate(
                EMPLOYEE_REGISTRATION_TEMPLATE,
                EmailConstant.SubjectName.EMPLOYEE_REGISTRATION_SUBJECT,
                Map.of(
                        EmailConstant.PropertyName.NAME_PROPERTY, data.getEmployeeName(),
                        EmailConstant.PropertyName.LINK_PROPERTY, registrationLink + data.getToken(),
                        EmailConstant.PropertyName.TTL_PROPERTY, data.getTtlInHours() == 1 ? data.getTtlInHours() + EmailConstant.HOUR : data.getTtlInHours() + EmailConstant.HOURS
                )
        );
    };
}
