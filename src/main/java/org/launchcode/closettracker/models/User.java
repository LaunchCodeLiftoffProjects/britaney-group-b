package org.launchcode.closettracker.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.launchcode.closettracker.data.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
public class User {

// Fields
    @Id
    @GeneratedValue
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @NotBlank
    private String firstName;

    @NotNull
    @NotBlank
    private String lastName;

    @NotNull
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String pwHash;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// Getters & Setters

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

// Constructors
    // Blank constructor needed, I think, for Spring to handle the database part
    public User() {}

    // Used by the CreateController
    public User(Integer userId, String firstName, String lastName, String email,
                String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.pwHash = encoder.encode(password);
    }

    // I feel like this constructor is needed only for logging in but not sure why yet
    public User(String username, String password) {
        this.username = username;
        this.pwHash = encoder.encode(password);
    }

    /* Need methods to:
        Find User object by username
        Check if provided password matches hashed password stored in user db
        Save user data to db
    */
    private UserRepository userRepository;
    // Retrieves
    public User getUserInfo(String username) {
        User user = userRepository.findByUsername(username);
        return user;
    }

    public boolean doesPasswordMatch(String password) {
        return encoder.matches(password, pwHash);
    }

}