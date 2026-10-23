package com.k955.Commune.exception;

import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends RuntimeException {
    private final ErrorCode code;

    public InvalidRefreshTokenException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

}
