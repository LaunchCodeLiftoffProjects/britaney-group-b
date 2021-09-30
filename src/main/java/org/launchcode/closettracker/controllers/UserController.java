package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.PasswordResetToken;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.*;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.launchcode.closettracker.repositories.PasswordTokenRepository;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
/*import org.springframework.security.core.context.SecurityContextHolder;*/

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.*;

import static org.launchcode.closettracker.controllers.HomeController.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private HomeController homeController;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private MailSender mailSender;

// Thymeleaf template page strings
    private static final String goUserCreate = "create";

// A function to generate a random string of letters and numbers
    public String createRandomString(int strLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = strLength;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

 /*   // get current users username - in progress

    public String currentUserName(HttpSession session) {
    User currentUser = homeController.getUserFromSession(session);
    currentUser.getUserName();
    }
*/
// CREATE START

    //localhost:8080/create
    @GetMapping("create")
    public String showCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create User Account");
        return goUserCreate;
    }

    // User --> Show create user form
    @PostMapping("create")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String processCreateAccountForm(@ModelAttribute @Valid UserDTO userDTO, Errors errors, HttpServletRequest request,
                                           Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Create User Account");
                return "create";
            }

            User currentUser = userRepository.findByEmail(userDTO.getEmail());

            if (currentUser != null) {
                errors.rejectValue("email", "email.exists", "An account with this email address already exists");
                model.addAttribute("title", "Create User Account");
                return "create";
            }

            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                errors.rejectValue("password", "passwords.nomatch", "Passwords do not match");
                model.addAttribute("pwdError", "Passwords do not match");
                model.addAttribute("title", "Create User Account");
                return "create";
            }

            User newUser = new User(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword(), false, true);
        // This line for debugging
            User activeUser = newUser;
            userRepository.save(newUser);
            return "redirect:";

        } catch (Exception ex) {
            if (ex.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "create";
        }
    }
// CREATE END

// RESET START

// Thymeleaf template page strings
    private static final String goUserReset1st = "user/reset";
    private static final String goUserReset2nd = "user/reset-int";
    public static final String goUserUpdate = "user/update";

// RECOVERY PART 1 - Reset password - enter email to generate token needed for step 2

// User --> Show email to reset form
    @GetMapping("user/reset")
    public String displayStartResetForm(Model model) {
        model.addAttribute(new ResetEmailDTO());
        model.addAttribute("title", "Reset Account Password");
        return goUserReset1st;
    }

// User --> Process email to reset form
    @PostMapping("user/reset")
    public String processStartResetForm(@ModelAttribute @Valid ResetEmailDTO resetEmailDTO, Errors errors, HttpServletRequest request, Model model) {
        User currentUser = userRepository.findByEmail(resetEmailDTO.getEmail());

    // Show any DTO validation errors
        if (errors.hasErrors()) {
            return goUserReset1st;
        }

    // If the user account does not exist, show error
        if (currentUser == null) {
            errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
            return goUserReset1st;
        }

    // Checks to see if current user has any existing reset tokens
        PasswordResetToken[] tokens = passwordTokenRepository.findAllByUser(currentUser);

    // Delete any previous tokens for the current user
        if (tokens != null) {
            for (int i = 0; i < tokens.length; i++) {
                PasswordResetToken actveToken = tokens[i];
                passwordTokenRepository.deleteById(tokens[i].getId());
            }
        }

    // Creates a (new) unique token string
        String token = UUID.randomUUID().toString();
    // Connects the above created token to the user and saves it to the token db
        createPasswordResetTokenForUser(currentUser, token);

    // While the User model does not persist the 'password' field, it is still required. So we need to...
        // 1) Since 'password' is still a required field, use a random string to set the password value and replace the hash
        currentUser.setPassword(createRandomString(8));
        // 2) To ensure the user will have to update their password upon next login attempt, set the flag to true
        currentUser.setPasswordReset(true);
        // 3) Persist the finished User object
        userRepository.save(currentUser);

    // Creates and sends an email to the user
    // For 'no outgoing email server error', add the Gmail SMTP outgoing email server credentials in the properties file
    // But don't forget to remove them before commit/push
    // The program will still run if no server is configured
        try {
            mailSender.send(constructResetTokenEmail(request.getLocale(), token, currentUser));
            model.addAttribute("message", "If your email address was found, you will receive a recovery email with further instructions.");
            return goUserReset2nd;
        }
        catch (Exception exception) {
            if (exception.toString().contains("not accepted")) {
                model.addAttribute("message", "Since no outgoing email server is configured, use the token shown below to complete the reset password process.");
                model.addAttribute("token", token);
                return goUserReset2nd;
            } else {
                errors.rejectValue("email", "some.unknownError", "An unknown error occurred.");
                return goUserReset1st;
            }
        }
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(user, token);
        passwordTokenRepository.save(myToken);
    }
// Creates the pieces needed for reset email construction
    private SimpleMailMessage constructResetTokenEmail(Locale locale, String token, User user) {
        String url = "http://localhost:8080/user/update?token=" + token;
        String message = "This automated message is to inform you that the password for your account at Closet Tracker has been reset." +
                "\n\nIf this was not you, then someone else has access to your account and that's not good." +
                "\n\nIf this was started by you then that's much better." +
                "\n\nYour unique reset token is: " + token + "\n\n" +
                "You can follow this link (http://localhost:8080/user/update) and enter the provided token manually to " +
                "reset your password or follow the link below to be taken to the same page with the token already filled in." +
                "\n\n(The prefill feature does not work yet. We sincerely apologize for the virtual inconvenience.)";
        return constructEmail("Your Closet Tracker password has been reset!", message + " \r\n\n" + url, user);
    }
// This function does the actual email construction
    private SimpleMailMessage constructEmail(String subject, String body,
                                             User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom("no.support@no.way");
        return email;
    }

// RECOVERY PART 2 - Update password - use token to choose a new password

// User --> Show update password form
    @GetMapping("user/update")
    public String showChooseNewPasswordForm(Model model, @RequestParam(value = "token", required = false) String token) {
        boolean result = validatePasswordResetToken(token);
        if(result) {
            model.addAttribute("token", token);
        }
        model.addAttribute(new UpdatePasswordDTO());
        return goUserUpdate;
    }

    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        if(passToken != null) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }

// User --> Process update password form
    @PostMapping("user/update")
    public String processChooseNewPasswordForm(@ModelAttribute @Valid UpdatePasswordDTO updatePasswordDTO, Errors errors,
                                            HttpServletRequest request, Model model) {

    // If reset token not found in db, display error message
        if(!validatePasswordResetToken(updatePasswordDTO.getToken())) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("token", "token.notValid", "Token is not valid. Please try again.");
            return goUserUpdate;
        }
    // If DTO validation errors, display error message(s)
        if (errors.hasErrors()) {
    // Unsure why it always clears the entered and confirm password fields
            model.addAttribute("updatePasswordDTO.passwordEntered", updatePasswordDTO.getPasswordEntered());
            model.addAttribute("updatePasswordDTO.passwordConfirm", updatePasswordDTO.getPasswordConfirm());
            return goUserUpdate;
        }

    // If both passwords do not match, display error message
        if (!updatePasswordDTO.getPasswordEntered().equals(updatePasswordDTO.getPasswordConfirm())) {
            model.addAttribute("passwordEntered", updatePasswordDTO.getPasswordEntered());
            model.addAttribute("passwordConfirm", updatePasswordDTO.getPasswordConfirm());
            errors.rejectValue("passwordConfirm", "passwordConfirm.notMatch", "Passwords do not match. Please try again.");
            return goUserUpdate;
        }

    // Retrieve users token object via the valid token string
        PasswordResetToken userByToken = passwordTokenRepository.findByToken(updatePasswordDTO.getToken());
    // Retrieve the user from the token
        User user = userRepository.findByEmail(userByToken.getUser().getEmail());

        if(user != null) {
        // Updates the user object password with the entered one
        // This process creates a new hash but does not persist the plain text password
            user.setPassword(updatePasswordDTO.getPasswordEntered());
        // Once password is successfully changed, set the reset flag to false allowing for normal login
            user.setPasswordReset(false);
        // Persists modified User object to db
            userRepository.save(user);
        // Once modified user object is saved, deletes the token from the token db
            passwordTokenRepository.deleteById(userByToken.getId());
        // Redirects user to login page
            model.addAttribute(new LoginFormDTO());
            model.addAttribute("title", "Welcome to Closet Tracker!");
            model.addAttribute("message", "Your password has successfully been reset. Login using your new password to access your account.");
            return "redirect:";
        }
        else {
        // If user is not found, displays error message
            errors.rejectValue("passwordEntered", "user.notFound", "No valid user found. Please try again.");
            return goUserUpdate;
        }
    }

// RESET END

// EDIT ACCOUNT START

// Thymeleaf template page strings
    private static final String goUserEditInfo = "user/edit-info";
    private static final String goUserEditPassword = "user/edit-password";

// User --> Show edit account info
    @GetMapping("user/edit-info")
    public String showEditAccountInfoForm(@ModelAttribute EditInfoDTO editInfoDTO,
                                          Errors errors, Model model, Model loginModel, HttpSession session) {
    // Get current user
        User currentUser = homeController.getUserFromSession(session);

    // If user object is null, redirect to login page
        if (currentUser == null) {
            loginModel.addAttribute("title", "Login");
            return "index";
        }
    // If DTO validation errors, display error message(s)
        if (errors.hasErrors()) {
            return goUserEditInfo;
        }

    // Set DTO fields with values from User db
        editInfoDTO.setUsername(currentUser.getUserName());
        editInfoDTO.setEmail(currentUser.getEmail());
        model.addAttribute(editInfoDTO);
        return goUserEditInfo;
    }

// User --> Process edit account info
    @PostMapping("user/edit-info")
    public String processEditAccountInfoForm(@ModelAttribute @Valid EditInfoDTO editInfoDTO, Errors errors,
                                             HttpServletRequest request, HttpSession session, Model model) {
/* Some cases to plan for:
    1) Since fields are prefilled with persisted info, when user hits Update compare the field values to the stored values, do nothing if same
    2) Since fields can be changed together or separately, check each as individual fields
    3) Username is not used for login so it can be whatever the user wants
    4) Email IS used for login so it must be unique - check email against db, then check userid vs currentUser. show error if not match
 */
    // Get current user
        User currentUser = homeController.getUserFromSession(session);

    // If the user account does not exist, redirect to login page as browser session has expired
        if (currentUser == null) {
            errors.rejectValue("email", "email.DoesNotExist", "An account with this email address does not exist");
            model.addAttribute("title", "Reset Account Password");
            return goUserEditInfo;
        }

    // If DTO validation errors, display error message(s)
        if (errors.hasErrors()) {
            // Unsure why it always clears the entered and confirm password fields
            return goUserEditInfo;
        }

    // Check if username has changed
        String activeUserName = editInfoDTO.getUsername();
        String currentUserName = currentUser.getUserName();
        boolean doUserNamesMatch = currentUserName.equals(activeUserName);
        boolean isUserNameChanged = false;
        boolean isEmailChanged = false;
        if (!currentUser.getUserName().equals(editInfoDTO.getUsername())) {
            model.addAttribute("message","No info has changed so you're all good!");
            isUserNameChanged = true;
            return goUserEditInfo;
        }
        currentUser.setUserName(editInfoDTO.getUsername());
        User activeUser = currentUser;
/*
        // Creates and sends an email to the user
        // If you receive an error about an outgoing email server not being configured, you need to add in the group Gmail
        // login credentials in the properties file
        try {
            mailSender.send(constructResetTokenEmail(request.getLocale(), null, currentUser));
        }
        catch (Exception exception) {
            if (exception.toString().contains("not accepted")) {
                errors.rejectValue("email", "server.notConfigured", "The password has been reset but no email was sent as there is no outgoing email server configured.");
            } else {
                errors.rejectValue("email", "some.unknownError", "An unknown error occurred.");
            }
            return goUserEditInfo;
        }
*/
// While the User model does not persist the 'password' field, it is still a required field for the user object. So...
        // 1) Since 'password' is still a required field, use a random string to set the password value and replace the hash
        currentUser.setPassword(createRandomString(8));
        // 2) To ensure the user will have to update their password upon next login, set the flag to true
        currentUser.setPasswordReset(true);
        // 3) Persist the finished User object
        userRepository.save(currentUser);

        model.addAttribute("message", "");
        return goUserEditInfo;
    }

// User --> Show edit password
    @GetMapping("user/edit-password")
    public String showEditPasswordForm(Model model) {
        model.addAttribute(new EditPasswordDTO());
        return goUserEditPassword;
    }

// EDIT ACCOUNT END

}
