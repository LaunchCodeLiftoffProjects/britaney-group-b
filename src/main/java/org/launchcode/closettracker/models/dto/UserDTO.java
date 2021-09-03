package org.launchcode.closettracker.models.dto;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserDTO {
    @NotNull(message = "User Name is required")
    @NotBlank(message = "User Name is required")
    @Column(name = "user_name", nullable = false)
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

    @NotNull(message = "Confirm Password is required")
    @NotBlank(message = "Confirm Password is required")
    @Size(min=3, max = 15,  message = "Confirm Password must be between 3 and 15 characters long")
    @Transient
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
