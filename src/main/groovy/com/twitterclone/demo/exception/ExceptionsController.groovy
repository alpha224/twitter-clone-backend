package com.twitterclone.demo.exception

import com.twitterclone.demo.exception.exceptions.*
import com.twitterclone.demo.utils.ApiUtils
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

import javax.servlet.http.HttpServletRequest

@Slf4j
@RestControllerAdvice
class ExceptionsController {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validationUserException(ValidationException e, HttpServletRequest request) {
        log.debug(e.getMessage(), e);
        return ApiUtils.buildErrorResponse(request, HttpStatus.BAD_REQUEST, e.getMessage())
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponse unauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        log.debug(e.getMessage(), e);
        return ApiUtils.buildErrorResponse(request, HttpStatus.UNAUTHORIZED, e.getMessage())
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse userNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        log.debug(e.getMessage(), e);
        return ApiUtils.buildErrorResponse(request, HttpStatus.NOT_FOUND, e.getMessage())
    }

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse userNotFoundException(PostNotFoundException e, HttpServletRequest request) {
        log.debug(e.getMessage(), e);
        return ApiUtils.buildErrorResponse(request, HttpStatus.NOT_FOUND, e.getMessage())
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse forbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.debug(e.getMessage(), e);
        return ApiUtils.buildErrorResponse(request, HttpStatus.FORBIDDEN, e.getMessage())
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleException(Throwable e, HttpServletRequest request) {
        log.error("Uncaught exception", e);
        return ApiUtils.buildErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "");
    }
}
