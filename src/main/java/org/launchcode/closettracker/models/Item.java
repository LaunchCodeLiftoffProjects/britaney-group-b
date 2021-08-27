package org.launchcode.closettracker.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Item extends AbstractEntity {

    private int id;

    @NotNull(message = "Name is required")
    @NotBlank(message = "Name is required")
    @Column(name = "item_name", nullable = false)
    private String itemName;

    private String type;

    private String color;

    private String size;

    private String season;

    private Date date;

    public Item(String itemName, String type, String color, String size, String season) {
        this.itemName = itemName;
        this.type = type;
        this.color = color;
        this.size = size;
        this.season = season;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }
}