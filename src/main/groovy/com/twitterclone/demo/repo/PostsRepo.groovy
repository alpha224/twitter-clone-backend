package com.twitterclone.demo.repo

import com.twitterclone.demo.repo.entities.Post
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PostsRepo extends MongoRepository<Post, String> {

    @Query(value = "{}", fields = "{postId : 1, owner : 1, data : 1, comments : 1, likedBy : 1}")
    List<Post> findAllWithCommentsAndLikesByOwnerId(String ownerId);

    @Query(value = "{}", fields = "{postId : 1, owner : 1, data : 1}")
    List<Post> findAllWithoutCommentsAndLikesByOwnerId(String ownerId);
}
