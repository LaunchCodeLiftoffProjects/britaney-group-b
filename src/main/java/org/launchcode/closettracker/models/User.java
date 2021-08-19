package org.launchcode.closettracker.models;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id", nullable = false)
    private int id;

    @NotNull(message = "First Name is required")
    @NotBlank(message = "First Name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull(message = "Last Name is required")
    @NotBlank(message = "Last Name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email(message = "Invalid email. Try again")
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Column(name = "user_name", unique = true)
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min=3, max = 15,  message = "Password must be between 3 and 15 characters long")
    @Transient
    private String password;

    @Column(name = "pw_hash")
    private String pwHash;


    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // CREATE: Capture user data to create a new account
    public  User(String firstname, String lastName, String email, String password) {
        this.firstName = firstname;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.pwHash = encoder.encode(password);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;

    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

// Constructors
    // Blank constructor needed, I think, for Spring to handle the database part
    public User() {}

    // Constructor for use by the Create controller
    public User(Integer userId, String firstName, String lastName, String email,
                String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.pwHash = encoder.encode(password);
    }

    // Constructor just for login info?
    public User(String username, String password) {
        this.username = username;
        this.pwHash = encoder.encode(password);
    }

    /* Need methods to:
        Find User object by username
        Check if provided password matches hashed password stored in user db
        Save user data to db
    */
    private UserRepository userRepository;
    // Retrieves
    public User getUserInfo(String username) {
        User user = userRepository.findByUsername(username);
        return user;
    }

    public int getId() {
        return id;
    }

    // Compare input password with its encoded password and assign it in pw_hash
    public boolean isEncodedPasswordEqualsInputPassword(String password) {
        String pwHashValue = encoder.encode(password);

        if(encoder.matches(password,pwHashValue)) {
            this.pwHash = pwHashValue;
            return true;
        }
        else
        {
            this.pwHash = null;
            return false;
        }
    }

    public User() {
    }
}
