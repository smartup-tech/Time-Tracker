package ru.smartup.timetracker.core.lock;

import java.util.concurrent.locks.Lock;

public interface LockManager {
    Lock getLock(String key);
}
