package org.launchcode.closettracker.models;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ClothSizes extends AbstractEntity {

    @NotBlank
    private String clothSize;

    @ManyToMany(mappedBy = "sizes")
    private final List<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return users;
    }

    public ClothSizes(String clothSize) {
        this.clothSize = clothSize;
    }

    public ClothSizes() {
    }

    public String getClothSize() {
        return clothSize;
    }

    public void setClothSize(String clothSize) {
        this.clothSize = clothSize;
    }
}
