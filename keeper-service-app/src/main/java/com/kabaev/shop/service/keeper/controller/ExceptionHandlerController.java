package com.kabaev.shop.service.keeper.controller;

import com.kabaev.shop.service.keeper.dto.ExceptionResponseDto;
import com.kabaev.shop.service.keeper.exception.ImageUploadException;
import com.kabaev.shop.service.keeper.exception.ProductExistsException;
import com.kabaev.shop.service.keeper.exception.ProductNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleProductNotFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(
                new ExceptionResponseDto(e.getMessage(), LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ProductExistsException.class)
    public ResponseEntity<ExceptionResponseDto> handleProductExistsException(Exception e) {
        return new ResponseEntity<>(
                new ExceptionResponseDto(e.getMessage(), LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ExceptionResponseDto> handleImageUploadException(Exception e) {
        return new ResponseEntity<>(
                new ExceptionResponseDto(e.getMessage(), LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleUnexpectedExceptions(
            HttpServletRequest request,
            Exception e) {
        var id = UUID.randomUUID().toString();
        log.error("Unexpected error: [incidentId={}, url={}, method={}]",
                id, request.getRequestURL(), request.getMethod(), e);
        List<String> stackTrace = Arrays.stream(e.getStackTrace())
                .map(Objects::toString)
                .toList();
        return new ResponseEntity<>(
                new ExceptionResponseDto(
                        "Unexpected error: please contact support regarding incident " + id,
                        LocalDateTime.now(),
                        stackTrace),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
