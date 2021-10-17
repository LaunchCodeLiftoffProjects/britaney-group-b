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
import java.util.Optional;
import java.util.Random;

import static org.launchcode.closettracker.models.User.passwordFixForSaveEditInfo;

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
    public static String createRandomString(int strLength) {
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
        editInfoDTO.setUsername(currentUser.getUsername());
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

    // Check if username has changed
        String activeUserName = editInfoDTO.getUsername();
        String currentUserName = currentUser.getUsername();
        boolean doUserNamesMatch = currentUserName.equals(activeUserName);
    // TODO: Debug code
        boolean asdasdasdsad = doUserNamesMatch;
        boolean isUserNameChanged = false;
    // TODO: Debug code
        boolean asdasdasdd = isUserNameChanged;

        if (!currentUser.getUsername().equals(editInfoDTO.getUsername())) {
            isUserNameChanged = true;
        }

    // Check if email has changed
        String activeEmail = editInfoDTO.getEmail();
        String currentEmail = currentUser.getEmail();
        boolean isEmailChanged = false;
    // TODO: Debug code
        boolean asdasdsad = isEmailChanged;

        if (!currentUser.getEmail().equals(editInfoDTO.getEmail())) {
            isEmailChanged = true;
        }

    // TODO: Debug code
        User activeUser = currentUser;

    // Before any actual updating takes place, need to verify that the changed email does not belong to another user account
        if (isEmailChanged) {
            User changedUser = userRepository.findByEmail(editInfoDTO.getEmail());
            if (changedUser == null) {
                //
            }
            else if (changedUser.getId() != currentUser.getId()) {
                errors.rejectValue("email", "user.invalid", "Not a valid user");
                model.addAttribute("message","That email is not available. Please try again.");
                return "user/edit-info";
            }
        }

        boolean groupChangeFlag = isUserNameChanged || isEmailChanged;
    // Now that changes are verified as valid and unique, load the user messages into the model based on change(s) made and return the page
        if (isUserNameChanged && isEmailChanged) {
            currentUser.setUsername(editInfoDTO.getUsername());
            currentUser.setEmail(editInfoDTO.getEmail());
            model.addAttribute("message","Username and email address have been successfully updated.");
        }
        else if (isUserNameChanged) {
            currentUser.setUsername(editInfoDTO.getUsername());
            model.addAttribute("message","Username has been successfully updated.");
        }
        else if (isEmailChanged) {
            currentUser.setEmail(editInfoDTO.getEmail());
            model.addAttribute("message","Email has been successfully updated.");
        }
        else {
            model.addAttribute("message","No info has changed so you're all good!");
        }
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
            return "user/edit-info";
        }
*/
    // While the User model does not persist the 'password' field, it is still a required field for the user object. So...
        // 1) Since 'password' is still a required field, call the special "fixer" function
        if (groupChangeFlag) {
            User verifiedUser = passwordFixForSaveEditInfo(currentUser);
        // TODO: Debug code
            User anotherActiveUser = verifiedUser;
        // Persist the finished User object
            userRepository.save(currentUser);
        }
        return "user/edit-info";
    }

// User --> Show edit password
    @GetMapping("user/edit-password")
    public String showEditPasswordForm(Model model, HttpSession session) {
        model.addAttribute(new EditPasswordDTO());
        model.addAttribute("phrase", loginController.getPhraseFromSession(session));
        return "user/edit-password";
    }

// User --> Process edit password
    @PostMapping("user/edit-password")
    public String processEditPasswordForm(@ModelAttribute @Valid EditPasswordDTO editPasswordDTO, Model model,
                                          HttpSession session, Errors errors) {

    // Get current user & that user's phrase
        User currentUser = loginController.getUserFromSession(session);
        String userDisplayPhrase = loginController.getPhraseFromSession(session);
    // Notes
        // 1) First check if the entered 'current' password matches stored password
        // 2) Next check if the new password matches the reentered new password
        // 3) If both checks pass, update the user object with the new password
        // 4) Persist




        errors.rejectValue("email", "user.invalid", "Not a valid user");
        model.addAttribute("message","That email is not available. Please try again.");

        return "user/edit-password";
    }
}
