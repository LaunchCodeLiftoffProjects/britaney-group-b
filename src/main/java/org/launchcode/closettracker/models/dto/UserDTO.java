package org.launchcode.closettracker.models.dto;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UserDTO extends LoginFormDTO {

    @NotBlank(message = "User Name is required")
    @Column(name = "user_name", nullable = false)
    private String username;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
