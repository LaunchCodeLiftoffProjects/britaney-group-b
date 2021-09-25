package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.*;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;

import static org.launchcode.closettracker.controllers.SessionController.goRedirectIndex;

@Controller
@RequestMapping("user")
public class CreateUserController {

    @Autowired
    private UserRepository userRepository;

    private SessionController sessionController;

// Thymeleaf page template strings
    public static final String goUserCreate = "user/create";

// User --> Show create user
    @GetMapping("create")
    public String displayCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create User Account");
        return goUserCreate;
    }

// User --> Process create user
    @PostMapping("create")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String createUser(@ModelAttribute @Valid UserDTO userDTO, Errors errors, HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Create User Account");
                /*model.addAttribute("errorMsg", "Bad data!");*/
                return goUserCreate;
            }

            User currentUser = userRepository.findByEmail(userDTO.getEmail());

            if (currentUser != null) {
                errors.rejectValue("email", "email.exists", "An account with this email address already exists");
                model.addAttribute("title", "Create User Account");
                return goUserCreate;
            }

            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                errors.rejectValue("password", "passwords.nomatch", "Passwords do not match");
                model.addAttribute("pwdError", "Passwords do not match");
                model.addAttribute("title", "Create User Account");
                return goUserCreate;
            }

            User newUser = new User(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword(), false, true);
            userRepository.save(newUser);
            return goRedirectIndex;

        } catch (Exception ex) {
            if (ex.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return goUserCreate;
        }
    }
}
