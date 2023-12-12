package com.twitterclone.demo.repo.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "posts")
class Post {
    @Id
    String postId;
    @DBRef
    User owner
    String data
    List<Comment> comments = []
    @DBRef
    List<User> likedBy = []
    long createDate
    long lastUpdate

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Post post = (Post) o

        if (createDate != post.createDate) return false
        if (lastUpdate != post.lastUpdate) return false
        if (data != post.data) return false
        if (owner != post.owner) return false
        if (postId != post.postId) return false

        return true
    }

    int hashCode() {
        int result
        result = postId.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + (data != null ? data.hashCode() : 0)
        result = 31 * result + (int) (createDate ^ (createDate >>> 32))
        result = 31 * result + (int) (lastUpdate ^ (lastUpdate >>> 32))
        return result
    }
}
