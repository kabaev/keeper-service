package com.kabaev.shop.service.keeper.controller;

import com.kabaev.shop.service.keeper.domain.ExceptionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponseDto> handleIllegalArgumentException(Exception e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new ExceptionResponseDto(e.getMessage(), LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleAllUnexpectedExceptions(Exception e) {
        log.error(e.getMessage(), e);
        List<String> stackTrace = Arrays.stream(e.getStackTrace())
                .map(Objects::toString)
                .collect(Collectors.toList());
        return new ResponseEntity<>(
                new ExceptionResponseDto(e.getMessage(), LocalDateTime.now(), stackTrace),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
