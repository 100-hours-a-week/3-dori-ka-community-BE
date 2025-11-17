package com.example.week7.common.exception.custom;

import com.example.week7.common.exception.ErrorMessage;

public class ResourceNotFoundException extends CustomException {

    public ResourceNotFoundException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
