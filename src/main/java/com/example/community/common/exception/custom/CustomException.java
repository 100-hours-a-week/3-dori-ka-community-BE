package com.example.community.common.exception.custom;

import com.example.community.common.exception.ErrorMessage;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorMessage errorMessage;

    public CustomException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }
}