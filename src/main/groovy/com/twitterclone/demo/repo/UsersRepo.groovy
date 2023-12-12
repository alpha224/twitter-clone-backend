package com.twitterclone.demo.repo

import com.twitterclone.demo.repo.entities.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepo extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username)
}
