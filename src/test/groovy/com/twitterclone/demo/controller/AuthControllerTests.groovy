package com.twitterclone.demo.controller

import com.twitterclone.demo.IntegrationTestsBase
import com.twitterclone.demo.auth.JwtTokenUtils
import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.service.AuthService
import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AuthControllerTests extends IntegrationTestsBase {

    @Autowired
    AuthService authService

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
    }

    def "testing user registration"() {
        given:
        UserDto userDto = new UserDto(username: "username", password: "password")

        long startDate = System.currentTimeMillis();

        when:
        def result = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/registration")
                        .content(JsonOutput.toJson(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
        )

        then:
        result.andExpect(MockMvcResultMatchers.status().isCreated())

        and:
        def id = UUID.fromString(getResultAsMap(result).get('id') as String)
        notThrown(IllegalArgumentException)

        and:
        def entity = usersRepo.findById(id.toString()).get()
        entity.username == userDto.username
        entity.password != userDto.password
        passEncoder.matches(userDto.getPassword(), entity.getPassword())
        entity.registrationDate >= startDate
        entity.lastUpdate >= startDate
    }

    def "testing user login"() {
        given:
        UserDto userDto = new UserDto(username: "username", password: "password")

        mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/registration")
                        .content(JsonOutput.toJson(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
        )

        when:
        String basicToken = String.format("Basic %s", new String(userDto.getUsername() + ":" + userDto.getPassword()).getBytes().encodeBase64())
        def res = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/login")
                        .header("Authorization", basicToken)
        )


        then:
        res.andExpect(MockMvcResultMatchers.status().isOk())
        String token = res.andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        JwtTokenUtils.isTokenValid(token)
    }

    def "testing user logout"() {
        given:
        UserDto userDto = new UserDto(username: "username", password: "password")

        def userRegRes = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/registration")
                        .content(JsonOutput.toJson(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
        )

        String userId = getResultAsMap(userRegRes).get("id")

        String basicToken = String.format("Basic %s", new String(userDto.getUsername() + ":" + userDto.getPassword()).getBytes().encodeBase64())
        def res = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/login")
                        .header("Authorization", basicToken)
        )

        String token = res.andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);

        when:
        def res1 = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        )

        def res2 = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        )

        def res3 = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .patch("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonOutput.toJson(userDto))
        )

        then:
        res1.andExpect(MockMvcResultMatchers.status().isOk())
        !authService.existsInCache(token)
        res2.andExpect(MockMvcResultMatchers.status().isUnauthorized())
        res3.andExpect(MockMvcResultMatchers.status().isUnauthorized())
    }
}
