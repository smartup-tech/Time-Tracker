package ru.smartup.timetracker.exception;

import lombok.Getter;
import ru.smartup.timetracker.dto.ErrorCode;

@Getter
public class InvalidParameterException extends RuntimeException {
    final ErrorCode errorCode;

    public InvalidParameterException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidParameterException(ErrorCode errorCode, String message, Throwable throwable) {
        super(message, throwable);
        this.errorCode = errorCode;
    }
}
