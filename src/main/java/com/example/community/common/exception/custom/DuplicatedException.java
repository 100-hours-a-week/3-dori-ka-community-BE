package com.example.community.common.exception.custom;

import com.example.community.common.exception.ErrorMessage;

public class DuplicatedException extends CustomException {

    public DuplicatedException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}