package org.launchcode.closettracker.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
public class User {

// Fields (only dealing with login/logout)
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    private String username;

    @NotNull
    private String pwHash;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// Getters & Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

// Constructors
    // Blank constructor required for something I don't remember what
    public User() {}

    // I feel like this constructor is needed but not sure why yet
    public User(String username, String password) {
        this.username = username;
        this.pwHash = encoder.encode(password);
    }

/*
    Since this only handles logging in, will need a method to get the
    User object by the username from the user db.
    It will be a User object but we only need to deal with the username/password fields
    But that method will have to also check the entered password against the hashed password
    I also think I'll need to create a repository for User to access the db
*/
    public getUserInfo(String username) {
        //
    }

    public boolean doesPasswordMatch(String password) {
        return encoder.matches(password, pwHash);
    }

}