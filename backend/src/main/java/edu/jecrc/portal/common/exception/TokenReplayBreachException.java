package edu.jecrc.portal.common.exception;

import org.springframework.http.HttpStatus;

public class TokenReplayBreachException extends ApiException {
    public TokenReplayBreachException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_REPLAY_BREACH_DETECTED");
    }
}
