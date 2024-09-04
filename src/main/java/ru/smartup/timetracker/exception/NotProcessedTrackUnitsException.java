package ru.smartup.timetracker.exception;

import lombok.Getter;
import ru.smartup.timetracker.dto.ErrorCode;

@Getter
public class NotProcessedTrackUnitsException extends RuntimeException {
    final ErrorCode errorCode;

    public NotProcessedTrackUnitsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
