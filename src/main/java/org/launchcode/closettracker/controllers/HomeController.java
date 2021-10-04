package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.UpdatePasswordDTO;
import org.launchcode.closettracker.models.dto.LoginFormDTO;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    public static final String userSessionKey = "user";
    public static final String userDisplayPhrase = "displayPhrase";

/* Thymeleaf global page template strings
    public static final String goIndex = "index";
    public static final String goRedirect = "redirect:";
    public static final String goRedirectIndex = "redirect:/index";
    private static final String goRedirectUserUpdate = "redirect:user/update";
    private static final String goRedirectUserCloset = "redirect:items/closet";
*/
// Function to retrieve userid from browser session
    public User getUserFromSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(userSessionKey);
        if (userId == null) {
            return null;
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return null;
        }

        return user.get();
    }

// Function to retrieve user's display name from browser session
    public String getPhraseFromSession(HttpSession session) {
        String phrase = (String) session.getAttribute(userDisplayPhrase);
        if (phrase == null || phrase.isEmpty()) {
            return "My Closet";
        }
        else {
            return phrase;
        }
    }

// setUserInSession now includes the users' phrase name along with user id
    public void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
        String phrase;
    // If displayPhrase is null, creates the displayName and displayPhrase for the user
        if (user.getDisplayPhrase() == null) {
            phrase = user.makeDisplayPhrase(user.makeDisplayName(user.getUserName()));
            user.setDisplayName(user.getUserName());
            user.setDisplayPhrase(user.getDisplayName());
        // Persists the updates to the user db
            userRepository.save(user);
        }
        else {
            phrase = user.getDisplayPhrase();
        }
        session.setAttribute(userDisplayPhrase, phrase);
    }

    protected String getUserDisplayName(HttpSession session) {
        User currentUser = getUserFromSession(session);
        if (currentUser != null) {
            String[] userDisplayName = currentUser.getUserName().split(" ");
            return userDisplayName[0] + "'s Closet";
        }
        else {
            return "My Closet";
        }
    }

    public String home(HttpServletResponse response) {
        //create a cookie with name 'website' and value 'javapointers'
        Cookie cookie = new Cookie("website", "javapointers");
        //set the expiration time
        //1 hour = 60 seconds x 60 minutes
        cookie.setMaxAge(60 * 60);
        //add the cookie to the  response
        response.addCookie(cookie);
        //return the jsp with the response
        return "home";
    }

// User --> Show login form
    @GetMapping("/index")
    public String index (Model model){
        model.addAttribute("title", "Welcome to Closet Tracker");
        model.addAttribute(new LoginFormDTO());
        return "index";
    }

// User --> Process login form
    @PostMapping("/index")
    public String processLoginForm(@ModelAttribute @Valid LoginFormDTO loginFormDTO, Errors errors,
                                   HttpServletRequest request, Model model){
        if (errors.hasErrors()) {
            model.addAttribute("title", "Welcome to Closet Tracker");
            return "index";
        }

        User theUser = userRepository.findByEmail(loginFormDTO.getEmail());

    // Is the user has reset their password, this checks to see if the flag is true.
    // If true, the user is redirected to the update page to choose a new password
        if (theUser.isPasswordReset()) {
            model.addAttribute(new UpdatePasswordDTO());
            return "redirect:/user/update";
        }

    // A final check to ensure the user exists in the db
        if (theUser == null) {
            errors.rejectValue("email", "user.invalid", "Not a valid user");
            model.addAttribute("title", "Welcome to Closet Tracker");
            return "index";
        }

        String password = loginFormDTO.getPassword();

    // I don't believe this works correctly; still allows log in with an incorrect password
        if (!theUser.isEncodedPasswordEqualsInputPassword(password)) {
            errors.rejectValue("password", "password.invalid", "Invalid password");
            model.addAttribute("title", "Welcome to Closet Tracker");
            return "index";
        }
        setUserInSession(request.getSession(), theUser);

        return "redirect:/items";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        return "redirect:";
    }

}
