package ru.smartup.timetracker.email.template.impl;

import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.EmailConstant;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.pojo.notice.NoticeFreeze;

import java.util.Map;

public class SuccessFreezeTemplate extends BaseEmailTemplate {
    private static final String SUCCESS_FREEZE_TEMPLATE = "successFreeze.html";

    @Override
    public EmailXmlTemplate getTemplate(final Notice notice) {
        return getUserRegistrationTemplateGenerator((NoticeFreeze) notice.getData());
    }

    private EmailXmlTemplate getUserRegistrationTemplateGenerator(final NoticeFreeze data) {
        return new EmailXmlTemplate(
                SUCCESS_FREEZE_TEMPLATE,
                EmailConstant.SubjectName.SUCCESS_FREEZE_SUBJECT,
                Map.of(
                        EmailConstant.PropertyName.FREEZING_TIMESTAMP_PROPERTY, data.getDate()
                )
        );
    };
}
