package ru.smartup.timetracker.core.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LockConfig {
    /**
     * При использовании распределенной системы заменить, например, на redis
     *
     * @return LockManager
     */
    @Bean
    public LockManager lockManager() {
        return new ReentrantLockManager();
    }
}
