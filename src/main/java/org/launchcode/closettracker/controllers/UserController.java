package org.launchcode.closettracker.controllers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserController {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
/*
    I feel like this controller should be the one to get info from the User view (login page)
    and check it and then redirect to the account page or say invalid and retry

    What should this controller do?
    - Capture login credentials from index.html
    - Verify if credentials match an existing user
        * Use doesPasswordMatch method in User model to verify if entered password and stored hash match
    - If so, redirect logged in page to index.html
    - If not, show an error message and ask to try again
 */
}
