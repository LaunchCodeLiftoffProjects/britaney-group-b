package org.launchcode.closettracker.models.dto;

import org.launchcode.closettracker.models.ClothSizes;
import org.launchcode.closettracker.models.User;

import javax.validation.constraints.NotNull;

public class UserClothingSizeDTO {

    @NotNull
    private User user;
    @NotNull
    private ClothSizes clothingSizes;

    public UserClothingSizeDTO() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ClothSizes getClothingSizes() {
        return clothingSizes;
    }

    public void setClothingSizes(ClothSizes clothingSizes) {
        this.clothingSizes = clothingSizes;
    }
}
