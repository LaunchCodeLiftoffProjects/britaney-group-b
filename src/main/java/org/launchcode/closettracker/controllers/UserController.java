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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("user/create")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String createUser(@ModelAttribute @Valid UserDTO userDTO, Errors errors, HttpServletRequest request, Model model) throws IOException {
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
                return "user/create";
            }

            User newUser = new User(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getPassword());
            userRepository.save(newUser);
            return "redirect:";

        } catch (Exception ex) {
            if (ex.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "user/create";
        }
    }

    @GetMapping("user/reset")
    public String displayResetPasswordForm(Model model) {
        model.addAttribute(new ResetDTO());
        model.addAttribute("title", "Reset Account Password");
        return "user/reset";
    }

    @PostMapping("user/reset")
    public String processResetPasswordForm(@ModelAttribute @Valid ResetDTO resetDTO, UserDTO userDTO, Errors errors, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Reset Account Password");
                model.addAttribute("errorMsg", "Info not correct.");
                return "user/reset";
            }

            User currentUser = userRepository.findByEmail(userDTO.getEmail());

            if (currentUser != null) {
                errors.rejectValue("email", "email.exists", "An account with this email address already exists");
                model.addAttribute("title", "Create User Account");
                return "user/reset";
            }

            if (!resetDTO.getPasswordEntered().equals(resetDTO.getPasswordConfirm())) {
                model.addAttribute("pwdError", "Passwords do not match. Please try again.");
                return "user/reset";
            }
//    userRepository.save(email);
            User newUser = new User(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getPassword());
            userRepository.save(newUser);
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
