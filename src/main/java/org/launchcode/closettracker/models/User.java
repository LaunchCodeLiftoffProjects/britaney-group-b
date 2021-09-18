package org.launchcode.closettracker.models;

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

/*    @ManyToMany
    private final List<Size> sizes = new ArrayList<>();

    public List<Size> getSizes() {
        return sizes;
    }

    public void AddSize(Size size)
    {
            this.sizes.add(size);
    }*/

//    @Column(name = "user_id", nullable = false)
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
    @Size(min=6, max = 24,  message = "Password must be between 6 and 24 characters long")
    @Transient
    private String password;

    @Column(name = "pw_hash")
    private String pwHash;

    @Column(name = "pw_reset")
    private boolean passwordReset;

    @Column(name = "new_user")
    private boolean isNewUser;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE: Capture user data to create a new account
    public User(String username, String email, String password, boolean pwReset, boolean newUser) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.pwHash = encoder.encode(password);
        this.passwordReset = pwReset;
        this.isNewUser = newUser;
    }

    public User(String password) {
        this.pwHash = encoder.encode(password);
        this.password = password;
        this.passwordReset = false;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) {
        this.password = password; this.pwHash = encoder.encode(password);
    }

    public String getPwHash() { return pwHash; }

    public void setPwHash(String password) { this.pwHash = pwHash; }

    public boolean isPasswordReset() { return passwordReset; }

    public boolean isNewUser() { return isNewUser; }

    public void setPasswordReset(boolean passwordReset) {
        this.passwordReset = passwordReset;
    }

    public void setNewUser(boolean newUser) { isNewUser = newUser; }

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
