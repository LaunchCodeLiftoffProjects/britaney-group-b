package org.launchcode.closettracker.models;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PasswordResetToken extends AbstractEntity{
    private static final int EXPIRATION = 60 * 24;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public int getId() {
        return id;
    }

    /*public void setId(int id) {
        this.id = id;
    }*/

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PasswordResetToken(User user, String token) {
        this.token = token;
        this.user = user;
    }

    public PasswordResetToken() {
    }
}
