package ru.smartup.timetracker.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.ResultDto;
import ru.smartup.timetracker.exception.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDto handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.NOT_VALID_DATA;
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse(StringUtils.EMPTY))));
    }

    @ExceptionHandler(InvalidParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDto handleInvalidParameterException(InvalidParameterException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultDto handleResourceNotFoundException(ResourceNotFoundException e) {
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDto handleBadCredentialsException(BadCredentialsException e) {
        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResultDto handleForbiddenException(ForbiddenException e) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(NotUniqueDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDto handleNotUniqueDataException(NotUniqueDataException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(NotProcessedTrackUnitsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDto handleNotProcessedTrackUnitsException(NotProcessedTrackUnitsException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(RelatedEntitiesFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDto handleRelatedEntitiesFoundException(RelatedEntitiesFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage(), e.getEntities());
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultDto handleSQLException(SQLException e) {
        ErrorCode errorCode = ErrorCode.DB_ERROR;
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ResultDto handleLockedException(LockedException e) {
        ErrorCode errorCode = ErrorCode.LOCKED;
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDto handleInvalidTokenException(InvalidTokenException e) {
        ErrorCode errorCode = ErrorCode.INVALID_PASSWORD_RECOVERY_TOKEN;
        log.error("An error occurred with code = " + errorCode + ".", e);
        return new ResultDto(errorCode, e.getMessage());
    }
}
