package org.launchcode.closettracker.models;

import org.launchcode.closettracker.controllers.EditUserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static org.launchcode.closettracker.controllers.EditUserController.createRandomString;

@Entity
public class User extends AbstractEntity {

    @NotNull(message = "Username is required")
    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false)
    private String username;

    @Email(message = "Invalid email. Try again")
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Column(name = "email", unique = true)
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    @Transient
    private String password;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "display_phrase")
    private String displayPhrase;

    @Column(name = "pw_hash")
    private String pwHash;

    @Column(name = "pw_reset")
    private boolean passwordReset;

    @Column(name = "new_user")
    private boolean isNewUser;

    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private final List<Item> items = new ArrayList<>();

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// CREATE: Capture user data to create a new account
    public User(String username, String email, String password, boolean pwReset, boolean newUser) {
        this.username = username;
        this.email = email;
        this.pwHash = encoder.encode(password);
        this.password = password;
        this.displayName = makeDisplayName(username);
        this.displayPhrase = makeDisplayPhrase(displayName);
        this.passwordReset = pwReset;
        this.isNewUser = newUser;
    }

    public User(String password) {
        this.pwHash = encoder.encode(password);
        this.passwordReset = false;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String username) { this.displayName = makeDisplayName(username); }

    public String getDisplayPhrase() { return displayPhrase; }

    public void setDisplayPhrase(String displayName) { this.displayPhrase = makeDisplayPhrase(displayName); }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public List<Item> getItems() { return items; }

    public String getPwHash() { return pwHash; }

    private void setPwHash(String pwHash) { this.pwHash = pwHash; }

    public void setPassword(String password) {
        this.password = password;
        this.pwHash = encoder.encode(password);
    }

    public boolean isPasswordReset() { return passwordReset; }

    public void setPasswordReset(boolean passwordReset) { this.passwordReset = passwordReset; }

    public boolean isNewUser() { return isNewUser; }

    public void setNewUser(boolean newUser) { isNewUser = newUser; }

// Compare input password with its encoded password and assign it in pw_hash
    public boolean isEncodedPasswordEqualsInputPassword(String password) {
        return encoder.matches(password, pwHash);
    }

    public User() {
    }

    public String makeDisplayName(String name) {
        String[] firstName = name.split(" ");
        return firstName[0];
    }

    public String makeDisplayPhrase(String name) {
        String[] lastLetter = name.split("");
        int ind = lastLetter.length - 1;
        String lastChar = lastLetter[ind].toLowerCase();
        if (lastChar.equals("s")) {
            return name + "'" + " Closet";
        }
        else {
            return name + "'s" + " Closet";
        }
    }

// Handles a fix to allow the user to keep their current password but allows the User object to save
// The User object is set to require a value for the "password" field
// The field is Transient and not persisted but is still required
// So this function, strictly limited to the User model class...
    public static User passwordFixForSaveEditInfo(User activeUser) {
    // Saves the users current password hash to a variable
        final String currentPwHash = activeUser.getPwHash();
    // Sets the password field to a random string so it has a value
        activeUser.setPassword(createRandomString(7));
    // Since just the user info has changed, resets the password hash back to the original so the user can still log in
        activeUser.setPwHash(currentPwHash);
    // Returns the currently active user with updated User object field values
        return activeUser;
    }

}
