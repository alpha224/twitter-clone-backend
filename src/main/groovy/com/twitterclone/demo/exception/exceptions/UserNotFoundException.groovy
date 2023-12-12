package com.twitterclone.demo.exception.exceptions

class UserNotFoundException extends RuntimeException {
    UserNotFoundException(String message) {
        super(message)
    }
}
