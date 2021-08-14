package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("createUser")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String createUser(@ModelAttribute @Valid User newUser, Errors errors, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Create User Account");
                model.addAttribute("errorMsg", "Bad data!");
                return "create";
            }

            if (newUser.getPassword().equals(newUser.getConfirmPassword())
                    & newUser.isEncodedPasswordEqualsInputPassword(newUser.getPassword())) {
                userRepository.save(newUser);
                return "redirect:";
            } else {
                model.addAttribute("pwdError", "Passwords do not match");
                return "create";
            }
        }
        catch (Exception ex)
        {
            if(ex.toString().contains("constraint"))
            {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            }
            else
            {
                model.addAttribute("dbError", "Db Error");
            }
            return "create";
        }
    }

}
