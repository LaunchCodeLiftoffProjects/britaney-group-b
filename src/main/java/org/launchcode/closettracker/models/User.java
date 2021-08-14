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
    private int id;

    @NotNull(message = "First Name is required")
    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotNull(message = "Last Name is required")
    @NotBlank(message = "Last Name is required")
    private String lastName;

    @Email(message = "Invalid email. Try again")
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    @Transient
    private String password;

    @NotNull(message = "Confirm Password is required")
    @NotBlank(message = "Confirm Password is required")
    @Size(min=3, max = 15,  message = "Confirm Password must be between 3 and 15 characters long")
    @Transient
    private String confirmPassword;

    private String pwHash;


    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE: Capture user data to create a new account
    public  User(String firstname, String lastName, String email, String password, String confirmPassword ) {
        this.firstName = firstname;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.pwHash = encoder.encode(password);
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public int getId() {
        return id;
    }

    public void setEncodePassword(String password) {
        String pwHashValue = encoder.encode(password);

        if(encoder.matches(password,pwHashValue)) {
            this.pwHash = pwHashValue;
        }
        else
        {
            this.pwHash = null;
        }
    }

    public User() {
    }
}
