package org.launchcode.closettracker.controllers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserController {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
/*
    I feel like this controller should be the one to get info from the User view (login page)
    and check it and then redirect to the account page or say invalid and retry
 */
}
