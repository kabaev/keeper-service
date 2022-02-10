package com.kabaev.shop.service.keeper.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ExceptionResponseDto {
    private String message;
    private LocalDateTime timestamp;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> stackTrace;

    public ExceptionResponseDto(String message, LocalDateTime timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
