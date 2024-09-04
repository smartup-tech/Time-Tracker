package ru.smartup.timetracker.core;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.smartup.timetracker.core.freeze.ScheduleFreezeProperties;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class SchedulerConfig {
    private final ScheduleFreezeProperties scheduleFreezeProperties;
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setClock(Clock.system(scheduleFreezeProperties.getTimeZone()));
        threadPoolTaskScheduler.setThreadNamePrefix(
                "ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }
}
