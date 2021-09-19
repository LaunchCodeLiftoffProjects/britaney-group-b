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
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/")
public class UserController {

// Repositories
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private MailSender mailSender;

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
// CREATE START

// User > Create new account
    @GetMapping("create")
    public String displayCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create User Account");
        return "create";
    }

// User > Process new account
    @PostMapping("create")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String processCreateAccountForm(@ModelAttribute @Valid UserDTO userDTO, Errors errors, HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Create User Account");
                model.addAttribute("errorMsg", "Something went wrong, try again!");
                return "create";
            }

// Checks user db for match
            User currentUser = userRepository.findByEmail(userDTO.getEmail());

// If match is found, displays an error message
            if (currentUser != null) {
                errors.rejectValue("email", "email.exists", "An account with this email address already exists");
                model.addAttribute("title", "Create User Account");
                return "create";
            }

// If entered passwords don't match, display error message
            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
             //   errors.rejectValue("password", "passwords.nomatch", "Passwords do not match");
                model.addAttribute("pwdError", "Passwords do not match");
             //   model.addAttribute("title", "Create User Account");
                return "create";
            }

// When everything is fine, create a new user object
            User newUser = new User(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword(), false, true);

// Save the new user to the user db
            userRepository.save(newUser);

// Upon complete process, show closet page
         /*   model.addAttribute("items", itemRepository.findAll());
            model.addAttribute("title", "My Closet");*/
            return "redirect:";

        } catch (Exception ex) {
            model.addAttribute("title", "Create User Account");
            if (ex.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "create";
        }
    }
// CREATE END

// ============================================================================================

// RESET START

// User --> Show reset password - Part 1 (enter email to generate token needed for step 2)
@GetMapping("user/reset")
public String displayPasswordResetForm(Model model) {
    model.addAttribute(new ResetEmailDTO());
    model.addAttribute("title", "Reset Account Password");
    return "user/reset";
}

// User --> Process new reset password
    @PostMapping("user/reset")
    public String processPasswordResetForm(@ModelAttribute @Valid ResetEmailDTO resetEmailDTO, Errors errors, HttpServletRequest request, Model model) {
        User currentUser = userRepository.findByEmail(resetEmailDTO.getEmail());

// If the user account does not exist, show error
        if (currentUser == null) {
            errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
            model.addAttribute("title", "Reset Account Password");
            return "user/reset";
        }

// Creates a unique token string
        String token = UUID.randomUUID().toString();

// Connects the above created token to the user and saves it to the token db
        createPasswordResetTokenForUser(currentUser, token);
// Creates and sends an email to the user
    // If you receive an error about an outgoing email server not being configured, you need to add in the group Gmail
        // login credentials in the properties file
        try {
            mailSender.send(constructResetTokenEmail(request.getLocale(), token, currentUser));
        }
        catch (Exception exception) {
            if (exception.toString().contains("not accepted")) {
                errors.rejectValue("email", "server.notConfigured", "The password has been reset but no email was sent as there is no outgoing email server configured.");
            } else {
                errors.rejectValue("email", "some.unknownError", "An unknown error occurred.");
            }
            return "user/reset";
        }

// While the User model does not persist the 'password' field, it is still required. So we need to...
    // 1) Since 'password' is still a required field, use a random string to set the password value and replace the hash
        currentUser.setPassword(createRandomString(8));
    // 2) To ensure the user will have to update their password upon next login, set the flag to true
        currentUser.setPasswordReset(true);
    // 3) Persist the finished User object
        userRepository.save(currentUser);

// Load the intermediate reset page
        return "user/reset-int";
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(user, token);
        passwordTokenRepository.save(myToken);
    }

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

    private SimpleMailMessage constructEmail(String subject, String body,
                                             User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom("no.support@no.way");
        return email;
    }

// UPDATE PASSWORD START - Recovery part 2

// User -> Update password
    @GetMapping("user/update")
    public String showChangePasswordForm(Model model, @RequestParam("token") String token) {
        boolean result = validatePasswordResetToken(token);
        if(result) {
            model.addAttribute("token", token);
        }
        model.addAttribute(new UpdatePasswordDTO());
        return "user/update";
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

    @PostMapping("user/update")
    public String processChangePasswordForm(@ModelAttribute @Valid UpdatePasswordDTO updatePasswordDTO, Errors errors,
                               HttpServletRequest request, Model model) {

// If the 1st password field is empty, display error message
        if (updatePasswordDTO.getPasswordEntered().isEmpty()) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordEntered", "passwordEntered.notMatch", "Passwords is required. Please try again.");
            return "user/update";
        }

// If the 2nd password field is empty, display error message
        if (updatePasswordDTO.getPasswordConfirm().isEmpty()) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordConfirm", "passwordConfirm.notMatch", "Password is required. Please try again.");
            return "user/update";
        }

// If both passwords do not match, display error message
        if (!updatePasswordDTO.getPasswordEntered().equals(updatePasswordDTO.getPasswordConfirm())) {
            model.addAttribute("passwordEntered", updatePasswordDTO.getPasswordEntered());
            model.addAttribute("passwordConfirm", updatePasswordDTO.getPasswordConfirm());
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordConfirm", "passwordConfirm.notMatch", "Passwords do not match. Please try again.");
            return "user/update";
        }

        if(!validatePasswordResetToken(updatePasswordDTO.getToken())) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("token", "token.equals", "Token is not valid. Please try again.");
            return "user/update";
        }
        int checkTokenSize = updatePasswordDTO.getToken().length();

        PasswordResetToken userByToken = passwordTokenRepository.findByToken(updatePasswordDTO.getToken());
        User user = userRepository.findByEmail(userByToken.getUser().getEmail());

        if(user != null) {
            user.setPassword(updatePasswordDTO.getPasswordEntered());
            user.setPasswordReset(false);
            userRepository.save(user);
            passwordTokenRepository.deleteById(userByToken.getId());
            model.addAttribute(new LoginFormDTO());
            model.addAttribute("title", "Welcome to Closet Tracker!");
            return "index";
        } else {
            model.addAttribute("title", "Update Account Password");
            model.addAttribute("pwdError", "User not found. Please try again.");
            return "user/update";
        }
   }

// UPDATE PASSWORD END

// RESET END

}