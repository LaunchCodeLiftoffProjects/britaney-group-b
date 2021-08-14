package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import javax.validation.Valid;
import java.io.IOException;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("createUser")
    public String createUser(@ModelAttribute @Valid User newUser, Errors errors, Model model) throws IOException {
        if (errors.hasErrors()) {
            model.addAttribute("title", "Create User Account");
            model.addAttribute("errorMsg", "Bad data!");
            return "create";
        }

        if (newUser.getPassword().equals(newUser.getConfirmPassword())) {
            newUser.setEncodePassword(newUser.getPassword());
            userRepository.save(newUser);
            return "redirect:";
        } else {
            model.addAttribute("pwdError", "Passwords do not match");
            return "create";
        }
    }

}
