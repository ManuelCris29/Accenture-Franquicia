package com.accenture.franquicia.exception;

import com.accenture.franquicia.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;


@RestControllerAdvice

public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)

    public Mono<ErrorResponse> handleNotFound(RuntimeException ex) {
        return Mono.just(new ErrorResponse("Not Found", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)

    public Mono<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return Mono.just(new ErrorResponse("Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGeneralError(Exception ex) {
        return Mono.just(new ErrorResponse("Error interno de el servidor", ex.getMessage()));
    }

}
