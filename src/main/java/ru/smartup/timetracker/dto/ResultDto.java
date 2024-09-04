package ru.smartup.timetracker.dto;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ResultDto {
    // глобальный статус
    private final boolean success;
    // дополнительные параметры в случае успеха
    private Map<String, String> params = Map.of();
    // код ошибки
    private ErrorCode errorCode;
    // глобальная ошибка
    private String errorMessage;
    // ошибки валидации входящих сообщений
    private Map<String, String> validationErrors = Map.of();
    // список связанных объектов
    List<?> relatedEntities = List.of();

    public ResultDto(Boolean success, Map<String, String> params) {
        this.success = success;
        this.params = params;
    }

    public ResultDto(ErrorCode errorCode, Map<String, String> validationErrors) {
        this.success = false;
        this.errorCode = errorCode;
        this.validationErrors = validationErrors;
    }

    public ResultDto(ErrorCode errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ResultDto(ErrorCode errorCode, String errorMessage, List<?> relatedEntities) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.relatedEntities = relatedEntities;
    }
}
