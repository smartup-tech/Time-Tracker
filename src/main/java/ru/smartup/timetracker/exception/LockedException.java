package ru.smartup.timetracker.exception;

public class LockedException extends RuntimeException {
    public LockedException(String message) {
        super(message);
    }
}
