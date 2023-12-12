package com.twitterclone.demo.controller

import com.twitterclone.demo.controller.dto.CommentDto
import com.twitterclone.demo.controller.dto.PostDto
import com.twitterclone.demo.exception.exceptions.ValidationException
import com.twitterclone.demo.service.PostsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("posts")
class PostsController {

    private final PostsService postsService

    PostsController(PostsService postsService) {
        this.postsService = postsService
    }

    @PostMapping("create/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new post", description = "Creates a new post for a user.")
    @ApiResponse(responseCode = "201", description = "Post created successfully", content = @Content(schema = @Schema(implementation = PostDto.class)))
    def createPost(@RequestBody PostDto postDto, @Parameter(description = "ID of the user") @PathVariable("userId") String userId) {
        validateNotEmpty(userId, "User ID")
        return postsService.createPost(postDto, userId)
    }

    @PatchMapping("{postId}")
    @Operation(summary = "Update a post", description = "Updates an existing post.")
    @ApiResponse(responseCode = "200", description = "Post updated successfully")
    def updatePost(@RequestBody PostDto postDto, @Parameter(description = "ID of the post") @PathVariable("postId") String postId) {
        validateNotEmpty(postId, "Post ID")
        postsService.updatePost(postDto, postId)
    }

    @DeleteMapping("{postId}")
    @Operation(summary = "Delete a post", description = "Deletes an existing post.")
    @ApiResponse(responseCode = "200", description = "Post deleted successfully")
    def deletePost(@Parameter(description = "ID of the post") @PathVariable("postId") String postId) {
        validateNotEmpty(postId, "Post ID")
        postsService.deletePost(postId)
    }

    @PutMapping("{postId}/like/{userId}")
    @Operation(summary = "Like a post", description = "Likes a post as a user.")
    @ApiResponse(responseCode = "200", description = "Post liked successfully")
    def likePost(@Parameter(description = "ID of the post") @PathVariable("postId") String postId, @Parameter(description = "ID of the user") @PathVariable("userId") String userId) {
        validateNotEmpty(postId, "Post ID")
        validateNotEmpty(userId, "User ID")
        postsService.likePost(postId, userId)
    }

    @PostMapping("{postId}/comment/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Comment on a post", description = "Adds a comment to a post as a user.")
    @ApiResponse(responseCode = "201", description = "Comment added successfully", content = @Content(schema = @Schema(implementation = CommentDto.class)))
    def commentPost(@RequestBody CommentDto commentDto, @Parameter(description = "ID of the post") @PathVariable("postId") String postId, @Parameter(description = "ID of the user") @PathVariable("userId") String userId) {
        validateNotEmpty(postId, "Post ID")
        validateNotEmpty(userId, "User ID")
        return postsService.commentPost(commentDto, postId, userId)
    }

    @GetMapping("{postId}/comments")
    @Operation(summary = "Get comments of a post", description = "Retrieves all comments for a post.")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully", content = @Content(schema = @Schema(implementation = CommentDto.class)))
    def getComments(@Parameter(description = "ID of the post") @PathVariable("postId") String postId) {
        validateNotEmpty(postId, "Post ID")
        postsService.getCommentsForPost(postId)
    }

    @GetMapping("myFeed")
    @Operation(summary = "Get my feed", description = "Retrieves feed for the current user.")
    @ApiResponse(responseCode = "200", description = "Feed retrieved successfully")
    def getMyFeed() {
        return postsService.getMyFeed()
    }

    @GetMapping("feed/{userId}")
    @Operation(summary = "Get user feed", description = "Retrieves feed for a specific user.")
    @ApiResponse(responseCode = "200", description = "Feed retrieved successfully")
    def getUserFeed(@Parameter(description = "ID of the user") @PathVariable("userId") String userId) {
        validateNotEmpty(userId, "User ID")
        return postsService.getUserFeed(userId)
    }

    private void validateNotEmpty(String value, String fieldName) {
        if (!value) {
            throw new ValidationException("$fieldName shouldn't be empty")
        }
    }
}