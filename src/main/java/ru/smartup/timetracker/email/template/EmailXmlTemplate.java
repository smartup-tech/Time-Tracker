package ru.smartup.timetracker.email.template;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class EmailXmlTemplate {
    private String templateName;
    private String templateSubject;
    private Map<String, Object> templateProperties;
}
