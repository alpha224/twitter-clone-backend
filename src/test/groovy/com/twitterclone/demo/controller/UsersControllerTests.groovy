package com.twitterclone.demo.controller

import com.twitterclone.demo.IntegrationTestsBase
import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.repo.entities.User
import groovy.json.JsonOutput
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class UsersControllerTests extends IntegrationTestsBase {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
    }

    def "test user update default scenario"() {
        given:
        UserDto userDto = new UserDto("username", "password")

        Map extractedMap = userRegistrationAndLogin(userDto)
        UUID userId = UUID.fromString(extractedMap.id as String)

        when:
        long date = System.currentTimeMillis()

        then:
        updateAndVerifyUser(userId, "newNick", "newPass", date, null, null)
    }

    def "test user update with null username and/or null pass and user not exists"() {
        given:
        UserDto userDto = new UserDto("username", "password")

        Map extractedMap = userRegistrationAndLogin(userDto)
        UUID userId = UUID.fromString(extractedMap.id as String)

        when:
        long date = System.currentTimeMillis()

        then:
        updateAndVerifyUser(userId, null, "newPass", date, userDto.getUsername(), null)

        and:
        updateAndVerifyUser(userId, "newNick", null, date, null, "password")

        and:
        updateAndVerifyUser(userId, null, null, date, "username", "password")

        and:

        def uuid = UUID.randomUUID()
        def res = mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/users/{userId}", uuid)
                        .content(JsonOutput.toJson(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
        )
        res.andExpect(MockMvcResultMatchers.status().isNotFound())

        getResultAsMap(res).get('message') == "User with id '" + uuid + "' not found"
    }

    def "test delete user default scenario"() {
        given:
        UserDto userDto = new UserDto("username", "password")

        Map extractedMap = userRegistrationAndLogin(userDto)
        UUID userId = UUID.fromString(extractedMap.id as String)

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.delete("/users/{userId}", userId))

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
        !usersRepo.findById(userId.toString()).isPresent()
    }

    def "test delete user when user doesn't exist"() {
        given:
        def uuid = UUID.randomUUID()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.delete("/users/{userId}", uuid))

        then:
        result.andExpect(MockMvcResultMatchers.status().isNotFound())

        getResultAsMap(result).get('message') == "User with id '" + uuid + "' not found"
    }

    def "test user following"() {
        given:
        String id1 = userRegistrationAndLogin(new UserDto(username: "user1", password: "password")).get("id")
        String id2 = userRegistrationAndLogin(new UserDto(username: "user2", password: "password")).get("id")

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.put("/users/{userId}/subscribe/{followerId}", id1, id2))
        def result2 = mockMvc.perform(MockMvcRequestBuilders.put("/users/{userId}/subscribe/{followerId}", id2, id1))

        then:
        result.andExpect(MockMvcResultMatchers.status().isCreated())
        usersRepo.findById(id1).get().getFollowers().size() == 1
        usersRepo.findById(id2).get().getSubscribers().size() == 1

        and:
        result2.andExpect(MockMvcResultMatchers.status().isCreated())
        usersRepo.findById(id2).get().getFollowers().size() == 1
        usersRepo.findById(id1).get().getSubscribers().size() == 1
    }

    def "test user unfollowing"() {
        given:
        String id1 = userRegistrationAndLogin(new UserDto(username: "user1", password: "password")).get("id")
        String id2 = userRegistrationAndLogin(new UserDto(username: "user2", password: "password")).get("id")

        mockMvc.perform(MockMvcRequestBuilders.put("/users/{userId}/subscribe/{followerId}", id1, id2))
        mockMvc.perform(MockMvcRequestBuilders.put("/users/{userId}/subscribe/{followerId}", id2, id1))

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.delete("/users/{userId}/subscribe/{followerId}", id1, id2))

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
        User user1 = usersRepo.findById(id1).get();
        User user2 = usersRepo.findById(id2).get();
        user1.getFollowers().size() == 0
        user1.getSubscribers().size() == 1
        user2.getFollowers().size() == 1
        user2.getSubscribers().size() == 0
    }
}
