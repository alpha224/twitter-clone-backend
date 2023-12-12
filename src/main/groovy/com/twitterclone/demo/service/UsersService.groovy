package com.twitterclone.demo.service

import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.UserNotFoundException
import com.twitterclone.demo.repo.UsersRepo
import com.twitterclone.demo.repo.entities.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UsersService {

    private final UsersRepo usersRepo
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()

    UsersService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo
    }

    Map<String, String> createUser(UserDto newUserDto) {
        String passEncoded = passwordEncoder.encode(newUserDto.getPassword())
        long time = System.currentTimeMillis()

        User user = new User(
                userId: UUID.randomUUID().toString(),
                username: newUserDto.getUsername(),
                password: passEncoded,
                registrationDate: time,
                lastUpdate: time
        )

        String id = usersRepo.save(user).getUserId()

        return Map.of("id", id)
    }

    void updateUser(UserDto userDto, String userId) {
        boolean hasChanges = false
        User user = checkIfUserExistsOrThrow(userId)

        if (userDto.getUsername()) {
            user.setUsername(userDto.getUsername())
            hasChanges = true
        }

        if (userDto.getPassword()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()))
            hasChanges = true
        }

        if (hasChanges) {
            user.setLastUpdate(System.currentTimeMillis())
            usersRepo.save(user)
        }
    }

    void deleteUser(String userId) {
        User user = checkIfUserExistsOrThrow(userId)
        usersRepo.delete(user)
    }

    void follow(String userId, String followerId) {
        User user = checkIfUserExistsOrThrow(userId)
        User follower = checkIfUserExistsOrThrow(followerId)

        user.followers << follower
        follower.subscribers << user

        usersRepo.saveAll([user, follower])
    }

    void unfollow(String userId, String followerId) {
        User user = checkIfUserExistsOrThrow(userId)
        User follower = checkIfUserExistsOrThrow(followerId)

        user.followers.remove(follower)
        follower.subscribers.remove(user)

        usersRepo.saveAll([user, follower])
    }

    void updateUserEntity(User user) {
        usersRepo.save(user)
    }

    User checkIfUserExistsOrThrow(String userId) {
        return getUserOrThrow(userId)
    }

    Optional<User> findUserByUsername(String username) {
        return usersRepo.findByUsername(username)
    }

    private User getUserOrThrow(String userId) {
        return usersRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + userId + "' not found"))
    }
}
