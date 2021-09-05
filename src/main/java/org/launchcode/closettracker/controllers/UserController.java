package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.ResetDTO;
import org.launchcode.closettracker.models.dto.UserDTO;
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

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

// User > Create new account
    @GetMapping("create")
    public String displayCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create New Account");
        return "user/create";
    }

// User > Process new account
    @PostMapping("create")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String processCreateAccountForm(@ModelAttribute @Valid UserDTO userDTO, Errors errors, HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Create User Account");
                model.addAttribute("errorMsg", "Bad data!");
                return "user/create";
            }

            User currentUser = userRepository.findByEmail(userDTO.getEmail());

            if (currentUser != null) {
                errors.rejectValue("email", "email.exists", "An account with this email address already exists");
                model.addAttribute("title", "Create User Account");
                return "user/create";
            }

            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                model.addAttribute("pwdError", "Passwords do not match");
                model.addAttribute("title", "Create User Account");
                return "user/create";
            }

            User newUser = new User(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword());
            userRepository.save(newUser);
            return "redirect:";

        } catch (Exception ex) {
            model.addAttribute("title", "Create User Account");
            if (ex.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "user/create";
        }
    }

// User > Reset password
    @GetMapping("reset")
    public String displayResetPasswordForm(Model model) {
        model.addAttribute(new ResetDTO());
        model.addAttribute("title", "Reset Account Password");
        return "user/reset";
    }

// User > Process reset password
    @PostMapping("reset")
//    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String processResetPasswordForm(@ModelAttribute @Valid ResetDTO resetDTO, Errors errors,
                                           HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Reset Account Password");
                model.addAttribute("errorMsg", "Info not correct.");
                return "user/reset";
            }

            User currentUser = userRepository.findByEmail(resetDTO.getEmail());
// If the user account does not exist, show error
            if (currentUser == null) {
                errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
                model.addAttribute("title", "Reset Account Password");
                return "user/reset";
            }


            if (!resetDTO.getPasswordEntered().equals(resetDTO.getPasswordConfirm())) {
                model.addAttribute("title", "Reset Account Password");
                model.addAttribute("pwdError", "Passwords do not match. Please try again.");
                return "user/reset";
            }

//            x
//            User newUser = new User(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getPassword());
//            userRepository.save(newUser);

            return "redirect:";

        } catch (Exception exception) {
            if (exception.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "user/reset";
        }
    }

    }