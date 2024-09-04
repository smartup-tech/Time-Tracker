package ru.smartup.timetracker.email.template.impl;

import ru.smartup.timetracker.email.template.BaseEmailTemplate;
import ru.smartup.timetracker.email.template.EmailConstant;
import ru.smartup.timetracker.email.template.EmailXmlTemplate;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.pojo.notice.NoticeUnfreeze;
import ru.smartup.timetracker.utils.DateUtils;

import java.util.Map;

public class UnfreezeHoursTemplate extends BaseEmailTemplate {
    private static final String UN_FREEZE_TEMPLATE = "unfreezeHours.html";

    @Override
    public EmailXmlTemplate getTemplate(final Notice notice) {
        return getUserRegistrationTemplateGenerator((NoticeUnfreeze) notice.getData());
    }

    private EmailXmlTemplate getUserRegistrationTemplateGenerator(final NoticeUnfreeze data) {
        return new EmailXmlTemplate(
                UN_FREEZE_TEMPLATE,
                EmailConstant.SubjectName.UN_FREEZE_SUBJECT,
                Map.of(
                        EmailConstant.PropertyName.UN_FREEZING_TIMESTAMP_PROPERTY, DateUtils.formatDate(data.getUnfreezeRecordDate()),
                        EmailConstant.PropertyName.FREEZING_TIMESTAMP_PROPERTY, data.getFreezeDate()
                )
        );
    };
}
