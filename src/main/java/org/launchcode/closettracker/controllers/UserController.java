package org.launchcode.closettracker.controllers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;

public class UserController {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
/*
    I feel like this controller should be the one to get info from the User view (login page)
    and check it and then redirect to the account page or say invalid and retry

    What should this controller do?
    - Capture login credentials from index.html upon submit
        * Get form field values of username and password
    - Verify if credentials match an existing user
        * Use doesPasswordMatch method in User model to verify if entered password and stored hash match
        - If so, show logged in page
            * Will need a template for that page but can redirect it to index.html or give it its own location
        - If not, show an error message and ask to try again
            * Show errors on same page, no need to redirect
 */

    public String displayAllCategories(Model model) {
        model.addAttribute("title", "All Categories");
        model.addAttribute("categories", eventCategoryRepository.findAll());
        return "eventCategories/index";
    }

}
