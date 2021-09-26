package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.PasswordResetToken;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.*;
import org.launchcode.closettracker.repositories.PasswordTokenRepository;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

import static org.launchcode.closettracker.controllers.SessionController.goRedirectIndex;

@Controller
@RequestMapping("user")
public class ResetUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private MailSender mailSender;

    private SessionController sessionController;

// Thymeleaf page template strings
    private static final String goUserReset1st = "user/reset/reset";
    private static final String goUserReset2nd = "user/reset/reset-int";
    private static final String goUserUpdate = "user/reset/update";

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

// RECOVERY PART 1 - Reset password - enter email to generate token needed for step 2

// User --> Show email to reset form
    @GetMapping("reset/reset")
    public String displayStartResetForm(Model model) {
        model.addAttribute(new ResetEmailDTO());
        model.addAttribute("title", "Reset Account Password");
        return goUserReset1st;
    }

// User --> Process email to reset form
    @PostMapping("reset/reset")
    public String processStartResetForm(@ModelAttribute @Valid ResetEmailDTO resetEmailDTO, Errors errors, HttpServletRequest request, Model model) {
        User currentUser = userRepository.findByEmail(resetEmailDTO.getEmail());

    // If the user account does not exist, show error
        if (currentUser == null) {
            errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
            model.addAttribute("title", "Reset Account Password");
            return goUserReset1st;
        }

    // Creates a unique token string
        String token = UUID.randomUUID().toString();
    // Delete any previous tokens for the user
        try {
            passwordTokenRepository.deleteById(currentUser.getId());
        }
        catch (Exception exception) {
            //
        }
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
            return goUserReset2nd;
        }
        catch (Exception exception) {
            if (exception.toString().contains("not accepted")) {
                errors.rejectValue("email", "server.notConfigured", "The password has been reset but no email was sent as there is no outgoing email server configured.");
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
    @GetMapping("reset/update")
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
    @PostMapping("reset/update")
    public String processChooseNewPasswordForm(@ModelAttribute @Valid UpdatePasswordDTO updatePasswordDTO, Errors errors,
                                            HttpServletRequest request, Model model) {

    // If the 1st password field is empty, display error message
        if (updatePasswordDTO.getPasswordEntered().isEmpty()) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordEntered", "passwordEntered.notMatch", "Passwords is required. Please try again.");
            return goUserUpdate;
        }

    // If the 2nd password field is empty, display error message
        if (updatePasswordDTO.getPasswordConfirm().isEmpty()) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordConfirm", "passwordConfirm.notMatch", "Password is required. Please try again.");
            return goUserUpdate;
        }

    // If both passwords do not match, display error message
        if (!updatePasswordDTO.getPasswordEntered().equals(updatePasswordDTO.getPasswordConfirm())) {
            model.addAttribute("passwordEntered", updatePasswordDTO.getPasswordEntered());
            model.addAttribute("passwordConfirm", updatePasswordDTO.getPasswordConfirm());
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordConfirm", "passwordConfirm.notMatch", "Passwords do not match. Please try again.");
            return goUserUpdate;
        }

    // If reset token not found in db, display error message
        if(!validatePasswordResetToken(updatePasswordDTO.getToken())) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("token", "token.equals", "Token is not valid. Please try again.");
            return goUserUpdate;
        }
        int checkTokenSize = updatePasswordDTO.getToken().length();

        PasswordResetToken userByToken = passwordTokenRepository.findByToken(updatePasswordDTO.getToken());
        User user = userRepository.findByEmail(userByToken.getUser().getEmail());

        if(user != null) {
        // Updates the user object password with the entered one
        // This process creates a new has but does not persist the plain text password
            user.setPassword(updatePasswordDTO.getPasswordEntered());
        // Once password is successfully changed, set the reset flag to false to allow for normal login
            user.setPasswordReset(false);
        // Persists modified User object to db
            userRepository.save(user);
        // Once modified user object is saved, deletes the token from the tokeb db
            passwordTokenRepository.deleteById(userByToken.getId());
        // Redirects user to login page
            model.addAttribute(new LoginFormDTO());
            model.addAttribute("title", "Welcome to Closet Tracker!");
            return goRedirectIndex;
        } else {
        // If user is not found, displays error message
            model.addAttribute("title", "Update Account Password");
            model.addAttribute("pwdError", "User not found. Please try again.");
            return goUserUpdate;
        }
    }

}
