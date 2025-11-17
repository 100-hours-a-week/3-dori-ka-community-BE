package com.example.week7.common.exception.custom;

import com.example.week7.common.exception.ErrorMessage;

public class DuplicatedException extends CustomException {

    public DuplicatedException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}