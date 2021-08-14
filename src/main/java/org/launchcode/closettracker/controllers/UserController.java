package org.launchcode.closettracker.controllers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserController {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

}
