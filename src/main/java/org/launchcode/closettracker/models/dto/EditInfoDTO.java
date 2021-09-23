package org.launchcode.closettracker.models.dto;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.validation.constraints.Email;

public class EditInfoDTO {

    private String username;

    @Email(message = "Invalid email. Try again")
    @Column(name = "email", unique = true)
    private String email;

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

}
