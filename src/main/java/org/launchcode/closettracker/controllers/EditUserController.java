package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.*;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Random;

@Controller
public class EditUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginController loginController;

    @Autowired
    private MailSender mailSender;

// Thymeleaf template page strings -- NOT YET IMPLEMENTED
    private static final String goUserCreate = "create";
    private static final String goUserEditInfo = "user/edit-info";
    private static final String goUserEditPassword = "user/edit-password";


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

// User --> Show edit account info
    @GetMapping("user/edit-info")
    public String showEditAccountInfoForm(@ModelAttribute EditInfoDTO editInfoDTO,
                                          Errors errors, Model model, Model loginModel, HttpSession session) {
    // Get current user
        User currentUser = loginController.getUserFromSession(session);

    // If user object is null, redirect to login page
        if (currentUser == null) {
            loginModel.addAttribute("title", "Login");
            return "index";
        }
    // If DTO validation errors, display error message(s)
        if (errors.hasErrors()) {
            return "user/edit-info";
        }

    // Set DTO fields with values from User db
        editInfoDTO.setUsername(currentUser.getUserName());
        editInfoDTO.setEmail(currentUser.getEmail());
        model.addAttribute(editInfoDTO);
        return "user/edit-info";
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
        User currentUser = loginController.getUserFromSession(session);

    // If the user account does not exist, redirect to login page as browser session has expired
        if (currentUser == null) {
            errors.rejectValue("email", "email.DoesNotExist", "An account with this email address does not exist");
            model.addAttribute("title", "Reset Account Password");
            return "user/edit-info";
        }

    // If DTO validation errors, display error message(s)
        if (errors.hasErrors()) {
            // Unsure why it always clears the entered and confirm password fields
            return "user/edit-info";
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
            return "user/edit-info";
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
        return "user/edit-info";
    }

// User --> Show edit password
    @GetMapping("user/edit-password")
    public String showEditPasswordForm(Model model) {
        model.addAttribute(new EditPasswordDTO());
        return "user/edit-password";
    }

}
