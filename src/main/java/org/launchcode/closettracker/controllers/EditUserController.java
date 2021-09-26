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

// User --> Process edit account info
    @PostMapping("edit/info")
    public String processEditAccountInfoForm(@ModelAttribute @Valid EditInfoDTO editInfoDTO, Errors errors,
                                             HttpServletRequest request, HttpSession session, Model model) {
        return goEditInfo;
    }

// User --> Show edit password
    @GetMapping("edit/password")
    public String showEditPasswordForm(Model model) {
        model.addAttribute(new EditPasswordDTO());
        return goEditPassword;
    }

    // User --> Process edit password
    @PostMapping("edit/password")
    public String processEditPasswordForm(Model model) {
        model.addAttribute(new EditPasswordDTO());
        return goEditPassword;
    }
}
