package org.launchcode.closettracker.models;

import org.launchcode.closettracker.controllers.EditUserController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User extends AbstractEntity {

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
    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    @Transient
    private String password;

    @Column(name = "pw_hash")
    private String pwHash;

    @Column(name = "pw_reset")
    private boolean passwordReset;

    @Column(name = "new_user")
    private boolean isNewUser;

    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private final List<Item> items = new ArrayList<>();

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE: Capture user data to create a new account
    public User(String username, String email, String password, boolean pwReset, boolean newUser) {
        this.username = username;
        this.email = email;
        this.pwHash = encoder.encode(password);
        this.password = password;
        this.passwordReset = pwReset;
        this.isNewUser = newUser;
    }

    public User(String password) {
        this.pwHash = encoder.encode(password);
        this.passwordReset = false;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public List<Item> getItems() { return items; }

    public String getPwHash() { return pwHash; }

    public void setPwHash(String pwHash) { this.pwHash = pwHash; }

    public void setPassword(String password) {
        this.password = password;
        this.pwHash = encoder.encode(password);
    }

    public boolean isPasswordReset() { return passwordReset; }

    public void setPasswordReset(boolean passwordReset) { this.passwordReset = passwordReset; }

    public boolean isNewUser() { return isNewUser; }

    public void setNewUser(boolean newUser) { isNewUser = newUser; }

    // Compare input password with its encoded password and assign it in pw_hash
    public boolean isEncodedPasswordEqualsInputPassword(String password) {
        return encoder.matches(password, pwHash);
    }

    public User() {
    }
}
