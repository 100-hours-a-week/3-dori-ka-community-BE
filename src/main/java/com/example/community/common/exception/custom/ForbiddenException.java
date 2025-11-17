package com.example.community.common.exception.custom;

import com.example.community.common.exception.ErrorMessage;

public class ForbiddenException extends CustomException {

    public ForbiddenException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}

