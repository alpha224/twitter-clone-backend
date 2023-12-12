package com.twitterclone.demo.controller.dto

import groovy.transform.Immutable

@Immutable
class PostViewDto {
    String data
    String ownerUsername
    String ownerId
    String createData
    String lastUpdate
    List<UserViewDto> likedBy
    List<CommentViewDto> comments
}
