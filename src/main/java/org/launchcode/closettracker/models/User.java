package org.launchcode.closettracker.models;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User extends AbstractEntity {

/*    @ManyToMany
    private final List<Size> sizes = new ArrayList<>();

    public List<Size> getSizes() {
        return sizes;
    }

    public void AddSize(Size size)
    {
            this.sizes.add(size);
    }*/

    @Column(name = "user_id", nullable = false)
    private int id;

    @NotNull(message = "Username is required")
    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false)
    private String username;

    @Email(message = "Invalid email. Try again")
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Column(name = "email", unique = true)
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @javax.validation.constraints.Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    @Transient
    private String password;

    @Column(name = "pw_hash")
    private String pwHash;


    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE: Capture user data to create a new account
    public  User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.pwHash = encoder.encode(password);
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    // Compare input password with its encoded password and assign it in pw_hash
    public boolean isEncodedPasswordEqualsInputPassword(String password) {
        String pwHashValue = encoder.encode(password);

        if(encoder.matches(password,pwHashValue)) {
            this.pwHash = pwHashValue;
            return true;
        }
        else
        {
            this.pwHash = null;
            return false;
        }
    }

    public User() {
    }
}
