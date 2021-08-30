package org.launchcode.closettracker.models.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LoginFormDTO {

    @NotNull(message = "Email is required")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password must not be blank")
    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    private String password;

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
}
