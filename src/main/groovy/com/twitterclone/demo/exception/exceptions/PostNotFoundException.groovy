package com.twitterclone.demo.exception.exceptions

class PostNotFoundException extends RuntimeException {
    PostNotFoundException(String message) {
        super(message)
    }
}
