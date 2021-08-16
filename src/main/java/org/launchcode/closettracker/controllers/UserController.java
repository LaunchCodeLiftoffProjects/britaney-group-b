package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.data.UserRepository;
import org.launchcode.closettracker.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

public class UserController {

    @Autowired
    private UserRepository userRepository;
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

/* Login flow:
    User enters username and password on login page
    User model checks for valid inputs
    If inputs valid, allow submit
    Upon allowed submit, capture user info and lookup user
    If user found, call doesPasswordMatch to verify entered password
    If no user found, display error message and redirect to login page
    If password match stored hash, record user session and load account page
    If password does not match, display error message and redirect to login page
 */


// If user session is valid, processes login and shows account page
    @PostMapping("login")
    // Since the model ensures the input is valid, this process may not need to check it a user exists?
    public String processLoginForm(@ModelAttribute @Valid User user,
                                       Errors errors, Model model) {

        User fetchUserInfo = user.getUserInfo(user.getUsername());

        public void validateUserInfo(String password) {
            if (user.doesPasswordMatch(password)) {

            }
        }
        if (errors.hasErrors()) {
            model.addAttribute("login", "Login");
            model.addAttribute(user);
            return "account";
        }

        return "redirect:";
    }

}
