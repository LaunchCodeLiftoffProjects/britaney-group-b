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
    private String confirmNewPassword;

    public String getCurrentPassword() { return currentPassword; }

    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }

    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmNewPassword() { return confirmNewPassword; }

    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
