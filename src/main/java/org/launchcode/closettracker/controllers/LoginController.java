package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.UpdatePasswordDTO;
import org.launchcode.closettracker.models.dto.UserDTO;
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
import java.util.Optional;

import static org.launchcode.closettracker.controllers.SessionController.userSessionKey;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

// Gets user browser session
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
// Sets user browser session
    private static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
    }

    //localhost:8080  Shows login form
    @GetMapping("/index")
    public String index (Model model){
        model.addAttribute("title", "Welcome to Closet Tracker");
        model.addAttribute(new LoginFormDTO());
        return "index";
    }

    @PostMapping("/index")
    public String processLoginForm(@ModelAttribute @Valid LoginFormDTO loginFormDTO, Errors errors,
                                   HttpServletRequest request, Model model){
        if (errors.hasErrors()) {
            model.addAttribute("title", "Welcome to Closet Tracker");
            errors.rejectValue("email", "email.reset", "The password for this account was reset so you must create a new password before logging in.");
            model.addAttribute("errorMsg", "Entry not valid!");
            return "index";
        }
        User theUser = userRepository.findByEmail(loginFormDTO.getEmail());

    // Is the user has reset their password, this checks to see if the flag is true.
    // If true, the user is redirected to the update page to choose a new password
        if (theUser.isPasswordReset()) {
    // May need to use this line instead of showing the update page as there is some issue with it showing but not working upon submit
            errors.rejectValue("username", "password.reset", "The password for this account was reset so you must create a new password before logging in.");
            model.addAttribute("title", "Update User Password");
            model.addAttribute(new UpdatePasswordDTO());
            return "redirect:user/update";
        }

        if (theUser == null) {
            errors.rejectValue("email", "user.invalid", "Not a valid user");
            model.addAttribute("title", "Welcome to Closet Tracker");
            return "index";
        }
        String password = loginFormDTO.getPassword();

        if (!theUser.isEncodedPasswordEqualsInputPassword(password)) {
            errors.rejectValue("password", "password.invalid", "Invalid password");
            model.addAttribute("title", "Welcome to Closet Tracker");
            return "index";
        }
        setUserInSession(request.getSession(), theUser);

        return "redirect:items/";
    }

    //localhost:8080/create
    @GetMapping("create")
    public String displayCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create User Account");
        return "create";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        return "redirect:";
    }


}
