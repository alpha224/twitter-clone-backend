package com.twitterclone.demo.repo.entities

import groovy.transform.Canonical
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Document(collection = "users")
@Canonical
class User implements UserDetails {
    @Id
    String userId;
    String username;
    String password;
    long registrationDate;
    long lastUpdate;
    @DBRef
    List<User> followers = []
    @DBRef
    List<User> subscribers = []
    @DBRef
    List<Post> posts = []

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        User user = (User) o

        if (lastUpdate != user.lastUpdate) return false
        if (registrationDate != user.registrationDate) return false
        if (password != user.password) return false
        if (userId != user.userId) return false
        if (username != user.username) return false

        return true
    }

    int hashCode() {
        int result
        result = userId.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + (int) (registrationDate ^ (registrationDate >>> 32))
        result = 31 * result + (int) (lastUpdate ^ (lastUpdate >>> 32))
        return result
    }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        return null
    }

    @Override
    boolean isAccountNonExpired() {
        return false
    }

    @Override
    boolean isAccountNonLocked() {
        return false
    }

    @Override
    boolean isCredentialsNonExpired() {
        return false
    }

    @Override
    boolean isEnabled() {
        return false
    }
}
