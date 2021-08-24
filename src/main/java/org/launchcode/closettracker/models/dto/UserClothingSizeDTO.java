package org.launchcode.closettracker.models.dto;

import org.launchcode.closettracker.models.Size;
import org.launchcode.closettracker.models.User;

import javax.validation.constraints.NotNull;

public class UserClothingSizeDTO {

    @NotNull
    private User user;
    @NotNull
    private Size clothingSizes;

    public UserClothingSizeDTO() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Size getClothingSizes() {
        return clothingSizes;
    }

    public void setClothingSizes(Size clothingSizes) {
        this.clothingSizes = clothingSizes;
    }
}
