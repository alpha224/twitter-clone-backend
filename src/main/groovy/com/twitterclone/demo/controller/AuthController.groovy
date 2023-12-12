package com.twitterclone.demo.controller

import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.ValidationException
import com.twitterclone.demo.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class AuthController {

    private final AuthService authService

    AuthController(AuthService authService) {
        this.authService = authService
    }

    @PostMapping("/login")
    @Operation(summary = "Log in a user", description = "Logs in a user and returns a JWT token.")
    @ApiResponse(responseCode = "200", description = "User logged in successfully")
    def login(HttpServletRequest request, HttpServletResponse response) {
        authService.login(request, response)
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out a user", description = "Logs out a user by invalidating the token.")
    @ApiResponse(responseCode = "200", description = "User logged out successfully")
    def logout(HttpServletRequest request) {
        authService.logout(request)
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", description = "Registers a new user and returns the user details.")
    @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid user details")
    def createUser(@RequestBody UserDto newUserDto) {
        if (!newUserDto.getUsername() || !newUserDto.getPassword()) {
            throw new ValidationException(String.format(
                    "%s shouldn't be empty", newUserDto.getUsername() ? "Password" : "Username"))
        }
        return authService.registration(newUserDto)
    }
}