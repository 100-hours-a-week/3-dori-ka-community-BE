package com.example.community.common.exception.custom;

import com.example.community.common.exception.ErrorMessage;

public class BadRequestException extends CustomException {

    public BadRequestException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
