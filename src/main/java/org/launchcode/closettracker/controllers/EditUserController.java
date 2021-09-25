package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.*;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.launchcode.closettracker.repositories.PasswordTokenRepository;
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

import static org.launchcode.closettracker.controllers.SessionController.goRedirect;
import static org.launchcode.closettracker.controllers.SessionController.goRedirectIndex;

@Controller
@RequestMapping("user")
public class EditUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailSender mailSender;

    private SessionController sessionController;

// Thymeleaf page template strings
    private static final String goEditInfo = "user/edit/info";
    private static final String goEditPassword = "user/edit/password";

// User --> Show edit account info
    @GetMapping("edit/info")
    public String showEditAccountInfoForm(@ModelAttribute EditInfoDTO editInfoDTO,
                                          Errors errors, Model model, Model loginModel, HttpSession session) {
    // Get current user
        User currentUser = sessionController.getUserFromSession(session);

    // If user object is null, redirect to login page
        if (currentUser == null) {
            loginModel.addAttribute("title", "Login");
            return goRedirectIndex;
        }

    // Set DTO fields with values from User db
        editInfoDTO.setUsername(currentUser.getUserName());
        editInfoDTO.setEmail(currentUser.getEmail());
        model.addAttribute(editInfoDTO);
        return goEditInfo;
    }

    public boolean checkIfUserEmailIsUnique(User currentUser, String changedEmail) {
        try {
            User editingUser = userRepository.findByEmail(changedEmail);
            int editingUserId = editingUser.id;
            int currentUserId = currentUser.id;
            if (editingUser == null || editingUserId == currentUserId) {
                return true;
            }
            else {
                return false;
            }
        } catch(Exception exception) {
            return false;
        }
    }

// User --> Process edit account info
    @PostMapping("edit/info")
    public String processEditAccountInfoForm(@ModelAttribute @Valid EditInfoDTO editInfoDTO, Errors errors,
                                             HttpServletRequest request, HttpSession session, Model model) {
/* Some cases to plan for:
    1) Since fields are prefilled with persisted info, when user hits Update compare the field values to the stored values, do nothing if same
    2) Since fields can be changed together or separately, check each as individual fields
    3) Username is not used for login so it can be whatever the user wants
    4) Email *IS* used for login, so it must be unique - check email against db, then check userid vs currentUser. show error if not match
*/
    // Get current user
        User currentUser = sessionController.getUserFromSession(session);
        boolean isUsernameChanged = false;
        boolean isEmailChanged = false;
    // If the user account does not exist, redirect to login page as browser session has expired
        if (currentUser == null) {
            errors.rejectValue("email", "user.DoesNotExist", "User is not logged in or user does not exist.");
            return goRedirectIndex;
        }
    // If username equals the stored username, display a message saying
        if (editInfoDTO.getUsername().equals(currentUser.getUserName())) {
            model.addAttribute("message", "Info did not change so you're all good!");
        }
    // If names are different, change that field and set changed flag to true
        else {
            currentUser.setUserName(editInfoDTO.getUsername());
            isUsernameChanged = true;
        }
    // If email equals the stored username, display a message saying
        if (editInfoDTO.getEmail().equals(currentUser.getEmail())) {
            model.addAttribute("message", "Info did not change so you're all good!");
        }
    // Call function to check email from edit form
        else if(checkIfUserEmailIsUnique(currentUser, editInfoDTO.getEmail())) {
            currentUser.setEmail(editInfoDTO.getEmail());
            isEmailChanged = true;
        }
        else {
            errors.rejectValue("email", "email.NotUnique", "The email entered cannot be used. Please try again.");
        }

        if (isUsernameChanged || isEmailChanged) {

            userRepository.save(currentUser);
            if (isUsernameChanged && isEmailChanged) {
                model.addAttribute("message", "Username and Email have been updated");
            } else if (isUsernameChanged) {
                model.addAttribute("message", "Username has been updated");
            } else {
                model.addAttribute("message", "Email has been updated");
            }
        }
        model.addAttribute("username", editInfoDTO.getUsername());
        model.addAttribute("email", editInfoDTO.getEmail());
        model.addAttribute("title", "Edit Account Information");
        return goEditInfo;
    }

    // User --> Show edit password
    @GetMapping("edit/password")
    public String showEditPasswordForm(Model model) {
        model.addAttribute(new EditPasswordDTO());
        return goEditPassword;
    }

// EDIT ACCOUNT END

}
