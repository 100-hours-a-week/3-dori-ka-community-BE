package com.example.week7.common.exception.custom;

import com.example.week7.common.exception.ErrorMessage;

public class ForbiddenException extends CustomException {

    public ForbiddenException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}

