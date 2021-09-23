package org.launchcode.closettracker.models.dto;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class EditPasswordDTO {

    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    private String currentPassword;

    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    private String newPassword;

    @Size(min=3, max = 15,  message = "Confirm Password must be between 3 and 15 characters long")
    private String confirmPassword;

    public String getCurrentPassword() {
        return password;
    }

    public void setCurrentPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return password;
    }

    public void setNewPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
