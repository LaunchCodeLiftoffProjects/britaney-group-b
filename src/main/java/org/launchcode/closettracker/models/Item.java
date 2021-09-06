package org.launchcode.closettracker.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
public class Item extends AbstractEntity{

    @NotNull(message = "Name is required")
    @NotBlank(message = "Name is required")
    @Column(name = "item_name", nullable = false)
    private String itemName;

    private String type;

    private Color color;

    private String size;

    private Season season;

    @Lob
    private byte[] itemImage;

    public Item(String itemName, String type, Color color, String size, Season season, byte[] itemImage) {
        this.itemName = itemName;
        this.type = type;
        this.color = color;
        this.size = size;
        this.season = season;
        this.itemImage = itemImage;
    }

    public Item() {
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public byte[] getItemImage() {
        return itemImage;
    }

    public void setItemImage(byte[] itemImage) {
        this.itemImage = itemImage;
    }
}