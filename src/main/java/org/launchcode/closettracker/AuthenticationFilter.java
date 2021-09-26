package org.launchcode.closettracker;

import org.launchcode.closettracker.controllers.LoginController;
import org.launchcode.closettracker.controllers.SessionController;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter implements HandlerInterceptor {

    @Autowired
    UserRepository userRepository;

    @Autowired
    LoginController homeController;

    SessionController sessionController;

    private static final List<String> whitelist = Arrays.asList("/user/reset/reset", "/user/reset/reset-int", "/user/reset/update",
            "/index", "/user/create", "/css");

    private static boolean isWhitelisted(String path) {
        for (String pathRoot : whitelist) {
            if (path.startsWith(pathRoot)) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

    // Don't require sign-in for whitelisted pages
        if (isWhitelisted(request.getRequestURI())) {
    // returning true indicates that the request may proceed
            return true;
        }
        else {
            HttpSession session = request.getSession();
            User user = sessionController.getUserFromSession(session);
        // The user is logged in
            if (user != null) {
                return true;
            }
            else {
        // The user is NOT logged in
                response.sendRedirect("/index");
                return false;
            }
        }
    }

}
