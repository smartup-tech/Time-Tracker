package ru.smartup.timetracker.email.template;

import ru.smartup.timetracker.entity.Notice;

public abstract class BaseEmailTemplate {

    public abstract EmailXmlTemplate getTemplate(final Notice notice);
}
