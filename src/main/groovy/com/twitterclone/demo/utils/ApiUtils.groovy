package com.twitterclone.demo.utils

import com.twitterclone.demo.exception.ErrorResponse
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletRequest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ApiUtils {

    static def formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    static ErrorResponse buildErrorResponse(HttpServletRequest request, HttpStatus httpStatus, String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError(httpStatus.getReasonPhrase());
        errorResponse.setStatus(httpStatus.value());
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(Instant.now().toString());
        errorResponse.setPath(request.getRequestURL().toString());
        return errorResponse;
    }

    static String convertEpochToHumanDate(long epoch) {
        def instant = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
}
