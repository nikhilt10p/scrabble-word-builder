package com.hasbropulse.scrabble.exception;

// This is thrown when the request violates input rules such as bad rack size, tile limit exceeded, non-alpha chars, etc
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
