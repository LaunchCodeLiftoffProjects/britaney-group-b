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
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Optional;

import static org.launchcode.closettracker.controllers.SessionController.goRedirect;

@Controller
@RequestMapping("user")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    private SessionController sessionController;

// Thymeleaf template page strings
    private static final String goIndex = "index";

    private static final String redirectUserUpdate = "redirect:user/update";

    private static final String redirectItemCloset = "redirect:items/";

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
        return goIndex;
    }

// User --> Process login form
    @PostMapping("/index")
    public String processLoginForm(@ModelAttribute @Valid LoginFormDTO loginFormDTO, Errors errors,
                                   HttpServletRequest request, Model model){
        if (errors.hasErrors()) {
            model.addAttribute("title", "Welcome to Closet Tracker");
            model.addAttribute("errorMsg", "Entry not valid!");
            return goIndex;
        }
        User theUser = userRepository.findByEmail(loginFormDTO.getEmail());

    // Is the user has reset their password, this checks to see if the flag is true.
    // If true, the user is redirected to the update page to choose a new password
        if (theUser.isPasswordReset()) {
    // May need to use this line instead of showing the update page as there is some issue with it showing but not working upon submit
            errors.rejectValue("email", "password.reset", "The password for this account was reset so you must create a new password before logging in.");
            model.addAttribute("title", "Update User Password");
            model.addAttribute(new UpdatePasswordDTO());
            return redirectUserUpdate;
        }

        if (theUser == null) {
            errors.rejectValue("email", "user.invalid", "Not a valid user");
            model.addAttribute("title", "Welcome to Closet Tracker");
            return goIndex;
        }

        String password = loginFormDTO.getPassword();

        if (!theUser.isEncodedPasswordEqualsInputPassword(password)) {
            errors.rejectValue("password", "password.invalid", "Invalid password");
            model.addAttribute("title", "Welcome to Closet Tracker");
            return goIndex;
        }
        sessionController.setUserInSession(request.getSession(), theUser);

        return redirectItemCloset;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        return goRedirect;
    }


}
