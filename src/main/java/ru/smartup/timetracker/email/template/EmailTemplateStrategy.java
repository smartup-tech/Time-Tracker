package ru.smartup.timetracker.email.template;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;

import java.util.Map;

@Component
public class EmailTemplateStrategy {
    @Qualifier("emailTemplates")
    private final Map<NoticeTypeEnum, BaseEmailTemplate> generators;

    public EmailTemplateStrategy(final Map<NoticeTypeEnum, BaseEmailTemplate> generators) {
        this.generators = generators;
    }

    public EmailXmlTemplate getTemplate(final Notice notice) {
        final BaseEmailTemplate tg = generators.get(notice.getType());
        if (tg != null) {
            return tg.getTemplate(notice);
        }
        return null;
    }
}
