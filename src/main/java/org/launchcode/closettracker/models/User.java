package org.launchcode.closettracker.models;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id", nullable = false)
    private int id;

    @NotNull(message = "First Name is required")
    @NotBlank(message = "First Name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull(message = "Last Name is required")
    @NotBlank(message = "Last Name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email(message = "Invalid email. Try again")
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Column(name = "user_name", unique = true)
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    @Transient
    private String password;

    @Column(name = "pw_hash")
    private String pwHash;

    private Boolean passwordReset = false;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE: Capture user data to create a new account
    public  User(String firstname, String lastName, String email, String password) {
        this.firstName = firstname;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.pwHash = encoder.encode(password);
    }

    public  User(String firstname, String lastName, String email, String password, Boolean passwordReset) {
        this.firstName = firstname;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.pwHash = encoder.encode(password);
        this.passwordReset = passwordReset;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(Boolean passwordReset) {
        this.passwordReset = passwordReset;
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
