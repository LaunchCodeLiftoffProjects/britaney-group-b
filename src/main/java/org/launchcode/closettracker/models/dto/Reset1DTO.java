package org.launchcode.closettracker.models.dto;

import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Reset1DTO {
    // Fields with annotations
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Email
    @Transient
    private String email;

    @NotNull(message = "Temporary password is required")
    @NotBlank(message = "Temporary password is required")
    @Transient
    private String tempPassword;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min=6, max = 24,  message = "Password must be between 6 and 24 characters long")
    @Transient
    private String passwordEntered;

    @NotNull(message = "Confirm password is required")
    @NotBlank(message = "Confirm password is required")
    @Size(min=6, max = 24,  message = "Password must be between 6 and 24 characters long")
    @Transient
    private String passwordConfirm;

    // Getters & Setters
    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getTempPassword() { return tempPassword; }

    public void setTempPassword(String email) { this.tempPassword = tempPassword; }

    public String getPasswordEntered() {
        return passwordEntered;
    }

    public void setPasswordEntered(String passwordEntered) {
        this.passwordEntered = passwordEntered;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
