package com.twitterclone.demo.repo.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef

class Comment {
    @Id
    String commentId
    String data
    @DBRef
    User owner
    @DBRef
    Post post
    long createDate
}
