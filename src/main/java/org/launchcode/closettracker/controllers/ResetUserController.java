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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

@Controller
public class ResetUserController {

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
}
