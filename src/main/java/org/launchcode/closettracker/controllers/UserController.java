package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.data.UserRepository;
import org.launchcode.closettracker.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
/*
    I feel like this controller should be the one to get info from the User view (login page)
    and check it and then redirect to the account page or say invalid and retry

    What should this controller do?
    - Capture login credentials from index.html upon submit
        * Get form field values of username and password
    - Verify if username matches an existing user
    - If so, verifies if password matches the stored hash of that user
        * Use doesPasswordMatch method in User model to verify if entered password and stored hash match
        - If so, show logged in page
            * Will need a template for that page but can redirect it to index.html or give it its own location
        - If not, show an error message and ask to try again
            * Show errors on same page, no need to redirect
 */

    public void getUserInfo() {
        if (userRepository) {
            userRepository.findByUsername(username);

        }
    }

    public void validateUserInfo() {
        if (User.doesPasswordsMatch(password)) {

        }
    }
/* Login flow:
    User enters username and password
    Upon submit, something checks for valid input
    If input valid, sends entered info to




 */


// If user session is valid, processes login and shows account page
    @PostMapping("login")
    public String processLoginForm(@ModelAttribute @Valid User user,
                                       Errors errors, Model model) {

        if (errors.hasErrors()) {
            model.addAttribute("login", "Create Tag");
            model.addAttribute(user);
            return "account";
        }

        return "redirect:";
    }

}
