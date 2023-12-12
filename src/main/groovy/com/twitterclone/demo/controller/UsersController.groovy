package com.twitterclone.demo.controller

import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.ValidationException
import com.twitterclone.demo.service.UsersService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UsersController {

    private final UsersService usersService

    UsersController(UsersService usersService) {
        this.usersService = usersService
    }

    @PatchMapping("{userId}")
    @Operation(summary = "Update user", description = "Updates a user's information.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    def updateUser(@RequestBody UserDto userDto, @Parameter(description = "ID of the user") @PathVariable("userId") String userId) {
        validateNotEmpty(userId, "User ID")
        usersService.updateUser(userDto, userId)
    }

    @DeleteMapping("{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user.")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    def deleteUser(@Parameter(description = "ID of the user") @PathVariable("userId") String userId) {
        validateNotEmpty(userId, "User ID")
        usersService.deleteUser(userId)
    }

    @PutMapping("{userId}/subscribe/{followerId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Follow user", description = "Starts following another user.")
    @ApiResponse(responseCode = "201", description = "User followed successfully")
    def follow(@Parameter(description = "ID of the user to follow") @PathVariable("userId") String userId,
               @Parameter(description = "ID of the follower") @PathVariable("followerId") String followerId) {
        validateNotEmpty(userId, "User ID")
        validateNotEmpty(followerId, "Follower ID")
        usersService.follow(userId, followerId)
    }

    @DeleteMapping("{userId}/subscribe/{followerId}")
    @Operation(summary = "Unfollow user", description = "Stops following another user.")
    @ApiResponse(responseCode = "200", description = "User unfollowed successfully")
    def unfollow(@Parameter(description = "ID of the user to unfollow") @PathVariable("userId") String userId,
                 @Parameter(description = "ID of the follower") @PathVariable("followerId") String followerId) {
        validateNotEmpty(userId, "User ID")
        validateNotEmpty(followerId, "Follower ID")
        usersService.unfollow(userId, followerId)
    }

    private static void validateNotEmpty(String value, String fieldName) {
        if (!value) {
            throw new ValidationException("$fieldName shouldn't be empty")
        }
    }
}