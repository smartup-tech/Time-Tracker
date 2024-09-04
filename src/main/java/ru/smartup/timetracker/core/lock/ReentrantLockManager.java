package ru.smartup.timetracker.core.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockManager implements LockManager {
    private final Map<String, Lock> locks;

    public ReentrantLockManager() {
        locks = new ConcurrentHashMap<>();
    }

    @Override
    public Lock getLock(String key) {
        if (locks.containsKey(key)) {
            return locks.get(key);
        }
        Lock reentrantLock = new ReentrantLock();
        Lock existReentrantLock = locks.putIfAbsent(key, reentrantLock);
        return existReentrantLock == null ? reentrantLock : existReentrantLock;
    }
}
