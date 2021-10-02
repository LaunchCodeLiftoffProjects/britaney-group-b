package org.launchcode.closettracker.models.dto;

import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginFormDTO {

    @Email(message = "Invalid email. Try again")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    @Transient
    private String password;

    public String getEmail() { return this.email; }

    public String getPassword() {
        return this.password;
    }

}
