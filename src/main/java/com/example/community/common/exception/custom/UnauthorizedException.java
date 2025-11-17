package com.example.community.common.exception.custom;

import com.example.community.common.exception.ErrorMessage;

public class UnauthorizedException extends CustomException{

    public UnauthorizedException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
