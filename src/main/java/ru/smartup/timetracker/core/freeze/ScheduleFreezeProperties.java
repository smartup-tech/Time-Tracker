package ru.smartup.timetracker.core.freeze;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.LocalTime;
import java.time.ZoneId;

@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "schedule.freeze")
@Getter
public class ScheduleFreezeProperties {
    private final ZoneId timeZone;
    private final LocalTime fixedTimeFreeze;
    private final int dayToUnfreeze;

    @ConstructorBinding
    public ScheduleFreezeProperties(final String timeZone, final int fixedTimeFreeze, final int dayToUnfreeze) {
        this.timeZone = ZoneId.of(timeZone);
        this.fixedTimeFreeze = LocalTime.ofSecondOfDay(fixedTimeFreeze);
        this.dayToUnfreeze = dayToUnfreeze;
    }
}
