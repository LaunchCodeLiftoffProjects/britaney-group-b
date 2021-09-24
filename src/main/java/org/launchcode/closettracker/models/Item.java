package org.launchcode.closettracker.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Item extends AbstractEntity{

    @NotNull(message = "Name is required")
    @NotBlank(message = "Name is required")
    @Column(name = "item_name", nullable = false)
    private String itemName;

    private String type;

    private Color color;

    private String size;

    private String[] season;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private String itemImage;

    public Item(String itemName, String type, Color color, String size, String[] season, String itemImage, User user) {
        this.itemName = itemName;
        this.type = type;
        this.color = color;
        this.size = size;
        this.season = season;
        this.itemImage = itemImage;
        this.user = user;
    }

    @Transient
    public String getImagePath() {
        if (itemImage == null) return null;

        return "/item-photos/" + id + "/" + itemImage;
    }

    public Item() {
    }

    public String getItemName() { return itemName; }

    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public Color getColor() { return color; }

    public void setColor(Color color) { this.color = color; }

    public String getSize() { return size; }

    public void setSize(String size) { this.size = size; }

    public String[] getSeason() { return season; }

    public void setSeason(String[] season) { this.season = season; }

    public String getItemImage() { return itemImage; }

    public void setItemImage(String itemImage) { this.itemImage = itemImage; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

}