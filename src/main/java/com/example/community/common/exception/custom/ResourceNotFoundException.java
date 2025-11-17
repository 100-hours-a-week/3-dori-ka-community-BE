package com.example.community.common.exception.custom;

import com.example.community.common.exception.ErrorMessage;

public class ResourceNotFoundException extends CustomException {

    public ResourceNotFoundException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
