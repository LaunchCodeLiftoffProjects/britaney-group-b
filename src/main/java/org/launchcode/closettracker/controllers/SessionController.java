package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Optional;

public class SessionController {

// Thymeleaf global page template strings
    public static final String goRedirect = "redirect:";
    public static final String goRedirectIndex = "redirect:/index";

    @Autowired
    private UserRepository userRepository;

    private static final String userSessionKey = "user";

    public User getUserFromSession(HttpSession session) {
        Optional<User> user = userRepository.findById((Integer) session.getAttribute(userSessionKey));
        if (user.isPresent()) {
            return user.get();
        }
        else {
            return null;
        }
    }

    protected void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
    }

}
