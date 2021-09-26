package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Optional;

//@Component
public class SessionController {

    @Autowired
    private static UserRepository userRepository;

    public static final String userSessionKey = "user";

    public static User getUserFromSession(HttpSession session) {
        Optional<User> user = userRepository.findById((Integer) session.getAttribute(userSessionKey));
        if (user.isPresent()) {
            return user.get();
        }
        else {
            return null;
        }
    }

    private static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
    }

}
