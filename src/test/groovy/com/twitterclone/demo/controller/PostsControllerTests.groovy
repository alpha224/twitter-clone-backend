package com.twitterclone.demo.controller

import com.twitterclone.demo.IntegrationTestsBase
import com.twitterclone.demo.controller.dto.CommentDto
import com.twitterclone.demo.controller.dto.CommentViewDto
import com.twitterclone.demo.controller.dto.PostDto
import com.twitterclone.demo.controller.dto.PostViewDto
import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.repo.entities.Comment
import com.twitterclone.demo.repo.entities.Post
import com.twitterclone.demo.repo.entities.User
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class PostsControllerTests extends IntegrationTestsBase {

    public static final String ID = "id"

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
    }

    def "test post create"() {
        given:
        long startDate = System.currentTimeMillis()
        String userId = userRegistrationAndLogin(new UserDto(username: "username", password: "password")).get(ID)
        PostDto postDto = new PostDto(data: "some text data")

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        then:
        result.andExpect(MockMvcResultMatchers.status().isCreated())

        and:
        def id = UUID.fromString(getResultAsMap(result).get('id') as String)
        notThrown(IllegalArgumentException)

        and:
        Post post = postsRepo.findById(id.toString()).get()
        User user = usersRepo.findById(userId).get()
        post.data == postDto.getData()
        post.owner == user
        post.createDate >= startDate
        post.lastUpdate >= startDate
        user.getPosts().contains(post)
    }

    def "test post update"() {
        given:
        long startDate = System.currentTimeMillis()
        String userId = userRegistrationAndLogin(new UserDto(username: "username", password: "password")).get(ID)
        PostDto postDto = new PostDto(data: "some text data")

        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        String postId = getResultAsMap(result).get(ID)

        PostDto newData = new PostDto("new text data")
        long updateTime = System.currentTimeMillis()
        when:
        result = mockMvc.perform(MockMvcRequestBuilders
                .patch("/posts/{postId}", postId)
                .content(JsonOutput.toJson(newData))
                .contentType(MediaType.APPLICATION_JSON)
        )

        then:

        result.andExpect(MockMvcResultMatchers.status().isOk())
        Post entity = postsRepo.findById(postId.toString()).get()
        User user = usersRepo.findById(userId).get()
        entity.data != postDto.getData()
        entity.data == newData.getData()
        entity.owner == user
        entity.createDate >= startDate && entity.createDate < updateTime
        entity.lastUpdate >= startDate && entity.lastUpdate >= updateTime
        user.getPosts().contains(entity)
    }

    def "test post delete"() {
        given:
        String userId = userRegistrationAndLogin(new UserDto(username: "username", password: "password")).get(ID)
        PostDto postDto = new PostDto(data: "some text data")

        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        String postId = getResultAsMap(result).get(ID)

        when:
        result = mockMvc.perform(MockMvcRequestBuilders
                .delete("/posts/{postId}", postId)
        )

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
        !postsRepo.findById(postId)
        usersRepo.findById(userId).get().getPosts().size() == 0
    }

    def "test post like"() {
        given:
        String userId = userRegistrationAndLogin(new UserDto(username: "username1", password: "password")).get(ID)
        String userId2 = userRegistrationAndLogin(new UserDto(username: "username2", password: "password")).get(ID)
        String userId3 = userRegistrationAndLogin(new UserDto(username: "username3", password: "password")).get(ID)

        PostDto postDto = new PostDto(data: "some text data")

        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        String postId = getResultAsMap(result).get(ID)

        when:

        mockMvc.perform(MockMvcRequestBuilders
                .put("/posts/{postId}/like/{userId}", postId, userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        mockMvc.perform(MockMvcRequestBuilders
                .put("/posts/{postId}/like/{userId}", postId, userId2)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        result = mockMvc.perform(MockMvcRequestBuilders
                .put("/posts/{postId}/like/{userId}", postId, userId3)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        Post post = postsRepo.findById(postId).get()

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk())

        post.getLikedBy().size() == 3
        for (String id : [userId, userId2, userId3]) {
            User user = usersRepo.findById(id).get();
            post.getLikedBy().contains(user)
        }
    }

    def "test post unlike"() {
        given:
        String userId = userRegistrationAndLogin(new UserDto(username: "username1", password: "password")).get(ID)
        String userId2 = userRegistrationAndLogin(new UserDto(username: "username2", password: "password")).get(ID)

        PostDto postDto = new PostDto(data: "some text data")

        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        String postId = getResultAsMap(result).get(ID)

        mockMvc.perform(MockMvcRequestBuilders
                .put("/posts/{postId}/like/{userId}", postId, userId2)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        Post post = postsRepo.findById(postId).get()
        User user = usersRepo.findById(userId2).get()
        assert post.getLikedBy().contains(user)
        assert post.getLikedBy().size() == 1

        when:

        result = mockMvc.perform(MockMvcRequestBuilders
                .put("/posts/{postId}/like/{userId}", postId, userId2)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        then:

        result.andExpect(MockMvcResultMatchers.status().isOk())
        !postsRepo.findById(postId).get().getLikedBy().contains(user)
    }

    def "test comment create"() {
        given:
        long startDate = System.currentTimeMillis()
        String userId = userRegistrationAndLogin(new UserDto(username: "username", password: "password")).get(ID)
        PostDto postDto = new PostDto(data: "some text data")


        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        String postId = getResultAsMap(result).get(ID)

        CommentDto commentDto = new CommentDto(data: "some comment text")

        when:

        result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/{postId}/comment/{userId}", postId, userId)
                .content(JsonOutput.toJson(commentDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        then:
        result.andExpect(MockMvcResultMatchers.status().isCreated())

        and:
        UUID.fromString(getResultAsMap(result).get('id') as String)
        notThrown(IllegalArgumentException)

        and:
        Post post = postsRepo.findById(postId).get()
        User user = usersRepo.findById(userId).get()
        post.data == postDto.getData()
        post.owner == user
        post.createDate >= startDate
        post.lastUpdate >= startDate
        user.getPosts().contains(post)
        post.getComments().size() == 1
        Comment comment = post.getComments().get(0);
        comment.owner == user
        comment.data == commentDto.getData()
        comment.post == post
        comment.createDate >= startDate
    }

    def "test get comments for post"() {
        given:
        String userId = userRegistrationAndLogin(new UserDto(username: "username", password: "password")).get(ID)
        String userId2 = userRegistrationAndLogin(new UserDto(username: "username2", password: "password")).get(ID)
        PostDto postDto = new PostDto(data: "some text data")

        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        String postId = getResultAsMap(result).get(ID)

        CommentDto commentDto = new CommentDto(data: "some comment text")

        mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/{postId}/comment/{userId}", postId, userId)
                .content(JsonOutput.toJson(commentDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/{postId}/comment/{userId}", postId, userId2)
                .content(JsonOutput.toJson(commentDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        when:
        result = mockMvc.perform(MockMvcRequestBuilders
                .get("/posts/{postId}/comments", postId)
        )

        List<CommentViewDto> comments = new JsonSlurper().parseText(result.andReturn().getResponse().getContentAsString()) as List<CommentViewDto>
        CommentViewDto comment1 = comments.get(0)
        CommentViewDto comment2 = comments.get(1)

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
        postsRepo.findById(postId).get().getComments().size() == 2
        comments.size() == 2

        and:
        comment1.getData() == commentDto.getData()
        comment1.getOwnerId() == userId
        comment1.getCreateDate()
        comment1.getOwnerUsername() == "username"

        and:
        comment2.getData() == commentDto.getData()
        comment2.getOwnerId() == userId2
        comment2.getCreateDate()
        comment2.getOwnerUsername() == "username2"
    }

    def "test myFeed and feed of another user"() {
        given:

        String userId = userRegistrationAndLogin(new UserDto(username: "username", password: "password")).get(ID)
        String userId2 = userRegistrationAndLogin(new UserDto(username: "username2", password: "password")).get(ID)

        PostDto postDto = new PostDto(data: "post1")

        //create post1
        def result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        String postId1 = getResultAsMap(result).get(ID)
        postDto = new PostDto(data: "post2")

        //create post2
        result = mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/create/{userId}", userId)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        String postId2 = getResultAsMap(result).get(ID)

        CommentDto commentDto = new CommentDto(data: "comment1")

        //create comment 1 user 1 post 1
        mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/{postId}/comment/{userId}", postId1, userId)
                .content(JsonOutput.toJson(commentDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        commentDto = new CommentDto(data: "comment2")

        //create comment 1 user 1 post 2
        mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/{postId}/comment/{userId}", postId2, userId)
                .content(JsonOutput.toJson(commentDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        commentDto = new CommentDto(data: "comment3")

        //create comment 1 user 2 post 1
        mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/{postId}/comment/{userId}", postId1, userId2)
                .content(JsonOutput.toJson(commentDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        commentDto = new CommentDto(data: "comment4")

        //create comment 1 user 2 post 2
        mockMvc.perform(MockMvcRequestBuilders
                .post("/posts/{postId}/comment/{userId}", postId2, userId2)
                .content(JsonOutput.toJson(commentDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        //like post 1 by user 2
        mockMvc.perform(MockMvcRequestBuilders
                .put("/posts/{postId}/like/{userId}", postId1, userId2)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        //like post 2 by user 2
        mockMvc.perform(MockMvcRequestBuilders
                .put("/posts/{postId}/like/{userId}", postId2, userId2)
                .content(JsonOutput.toJson(postDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        //follow user2 -> user1
        mockMvc.perform(MockMvcRequestBuilders.put("/users/{userId}/subscribe/{followerId}", userId2, userId))


        when:
        UserDto userDto = new UserDto(username: "username2", password: "password")

        String basicToken = String.format("Basic %s", new String(userDto.getUsername() + ":" + userDto.getPassword()).getBytes().encodeBase64())
        def tokenResult = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/login")
                        .header(HttpHeaders.AUTHORIZATION, basicToken)
        )

        String token = tokenResult.andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);

        //get feed for user that logged
        def result1 = mockMvcWithFilter.perform(MockMvcRequestBuilders
                .get("/posts/myFeed")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(JsonOutput.toJson(userDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        def feed = new JsonSlurper().parseText(result1.andReturn().getResponse().getContentAsString()) as List<PostViewDto>

        userDto = new UserDto(username: "username", password: "password")

        basicToken = String.format("Basic %s", new String(userDto.getUsername() + ":" + userDto.getPassword()).getBytes().encodeBase64())

        tokenResult = mockMvcWithFilter.perform(
                MockMvcRequestBuilders
                        .post("/login")
                        .header(HttpHeaders.AUTHORIZATION, basicToken)
        )

        token = tokenResult.andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);

        //get feed for user that logged
        def result2 = mockMvcWithFilter.perform(MockMvcRequestBuilders
                .get("/posts/myFeed")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(JsonOutput.toJson(userDto))
                .contentType(MediaType.APPLICATION_JSON)
        )

        def feed2 = new JsonSlurper().parseText(result2.andReturn().getResponse().getContentAsString()) as List<PostViewDto>


        def result3 = mockMvc.perform(MockMvcRequestBuilders
                .get("/posts/feed/{userId}", userId2)
                .content(JsonOutput.toJson(userDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        def feed3 = new JsonSlurper().parseText(result3.andReturn().getResponse().getContentAsString()) as List<PostViewDto>


        then:
        result1.andExpect(MockMvcResultMatchers.status().isOk())

        feed.size() == 2

        feed.get(0).comments.size() == 2
        feed.get(0).likedBy.size() == 1

        feed.get(1).comments.size() == 2
        feed.get(1).likedBy.size() == 1

        and:
        result2.andExpect(MockMvcResultMatchers.status().isOk())
        feed2.size() == 0

        and:
        result3.andExpect(MockMvcResultMatchers.status().isOk())
        feed3.size() == 2

        !feed3.get(0).comments
        !feed3.get(0).likedBy

        !feed3.get(1).comments
        !feed3.get(1).likedBy
    }
}
