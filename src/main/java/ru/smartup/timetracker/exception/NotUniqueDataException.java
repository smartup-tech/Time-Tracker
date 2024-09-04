package ru.smartup.timetracker.exception;

import lombok.Getter;
import ru.smartup.timetracker.dto.ErrorCode;

@Getter
public class NotUniqueDataException extends RuntimeException {
    final ErrorCode errorCode;

    public NotUniqueDataException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
