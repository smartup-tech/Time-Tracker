package ru.smartup.timetracker.core.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.ConstructorBinding;

@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.mail")
@ConfigurationPropertiesScan
@ConstructorBinding
@Getter
public class EmailNotifierProperties {
    private final String username;
}
