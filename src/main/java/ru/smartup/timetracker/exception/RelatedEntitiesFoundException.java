package ru.smartup.timetracker.exception;

import lombok.Getter;
import ru.smartup.timetracker.dto.ErrorCode;

import java.util.List;

@Getter
public class RelatedEntitiesFoundException extends RuntimeException {
    final ErrorCode errorCode;
    final List<?> entities;

    public RelatedEntitiesFoundException(ErrorCode errorCode, String message, List<?> entities) {
        super(message);
        this.errorCode = errorCode;
        this.entities = entities;
    }
}
