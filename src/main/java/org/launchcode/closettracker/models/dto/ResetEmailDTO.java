package org.launchcode.closettracker.models.dto;

import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ResetEmailDTO {
    // Fields with annotations
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Email
    @Transient
    private String email;

    // Getters & Setters
    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

}