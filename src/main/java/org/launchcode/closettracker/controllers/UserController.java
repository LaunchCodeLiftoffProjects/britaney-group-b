package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.PasswordResetToken;
import org.launchcode.closettracker.models.GenericResponse;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.*;
import org.launchcode.closettracker.repositories.PasswordTokenRepository;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private MailSender mailSender;

// CREATE START
// User > Create new account
    @GetMapping("create")
    public String displayCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create New Account");
        return "user/create";
    }

// User > Process new account
    @PostMapping("create")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String processCreateAccountForm(@ModelAttribute @Valid UserDTO userDTO, Errors errors, HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Create User Account");
                model.addAttribute("errorMsg", "Bad data!");
                return "user/create";
            }

// Checks user db for match
            User currentUser = userRepository.findByEmail(userDTO.getEmail());

// If match is found, aborts create new user
            if (currentUser != null) {
                errors.rejectValue("email", "email.exists", "An account with this email address already exists");
                model.addAttribute("title", "Create User Account");
                return "user/create";
            }

// If entered passwords don't match, show error and stop create
            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                model.addAttribute("pwdError", "Passwords do not match");
                model.addAttribute("title", "Create User Account");
                return "user/create";
            }

// When everything is fine, create a new user object
            User newUser = new User(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword());
// Save the new user to the user db
            userRepository.save(newUser);
// Upon complete process, show closet page
            return "items/closet";

        } catch (Exception ex) {
            model.addAttribute("title", "Create User Account");
            if (ex.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "user/create";
        }
    }
// CREATE END

// RESET START
// User > Show 1st reset password
    @GetMapping("reset")
    public String display1stResetPasswordForm(Model model) {
        model.addAttribute(new ResetDTO());
        model.addAttribute("title", "Reset Account Password");
        return "user/reset";
    }

// A little function to generate a temporary password
    public String createTemporaryPassword() {
        byte[] array = new byte[6]; // length is bounded by 8
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        return generatedString;
    }

// User > Process 1st reset password
    @PostMapping("reset")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String process1stResetPasswordForm(@ModelAttribute @Valid ResetDTO resetDTO, Errors errors,
                                           HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Reset Account Password");
                model.addAttribute("errorMsg", "Info not correct.");
                return "user/reset";
            }

            User currentUser = userRepository.findByEmail(resetDTO.getEmail());

// If the user account does not exist, show error
            if (currentUser == null) {
                errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
                model.addAttribute("title", "Reset Account Password");
                return "user/reset";
            }

//            User userToUpdate = userRepository.findById(currentUser.getId());

/* If the user exists:
    1) Change the password reset flag to true
    2) Generate a temporary password
    3) Convert the temporary password to a password hash
    4) Store it in the user db
    5) Send an email
 */
            // 1
            currentUser.setPasswordReset(true);
            // 2 & 3
//                String password = createTemporaryPassword();
//                currentUser.setPwHash(password);
            // 4
                userRepository.save(currentUser);
            // 5
            User savedUser = userRepository.findByEmail(currentUser.getEmail());

                return "index";

        } catch (Exception exception) {
            if (exception.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "user/reset";
        }

    }

    // User > Show 2nd reset password
    @GetMapping("reset1")
    public String display2ndResetPasswordForm(Model model) {
        model.addAttribute(new ResetDTO());
        model.addAttribute("title", "Reset Account Password");
        return "user/reset1";
    }

    // User > Process reset password
    @PostMapping("reset1")
//    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String process2ndResetPasswordForm(@ModelAttribute @Valid Reset1DTO reset1DTO, Errors errors,
                                           HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Reset Account Password");
                model.addAttribute("errorMsg", "Info not correct.");
                return "user/reset1";
            }

            User currentUser = userRepository.findByEmail(reset1DTO.getEmail());
// If the user account does not exist, show error
            if (currentUser == null) {
                errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
                model.addAttribute("title", "Reset Account Password");
                return "user/reset1";
            }


            if (!reset1DTO.getPasswordEntered().equals(reset1DTO.getPasswordConfirm())) {
                model.addAttribute("title", "Reset Account Password");
                model.addAttribute("pwdError", "Passwords do not match. Please try again.");
                return "user/reset1";
            }

//            x
//            User newUser = new User(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getPassword());
//            userRepository.save(newUser);

            return "redirect:";

        } catch (Exception exception) {
            if (exception.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return "user/reset1";
        }
    }
// RESET END v1
// ============================================================================================
// RESET START v2

// User --> Show new reset password
@GetMapping("reset2")
public String displayNewResetPasswordForm(Model model) {
    model.addAttribute(new ResetEmailDTO());
    model.addAttribute("title", "Reset Account Password");
    return "user/reset2";
}

// User --> Process new reset password
    @PostMapping("reset2")
    public String resetPassword(@ModelAttribute @Valid ResetEmailDTO resetEmailDTO, Errors errors, HttpServletRequest request, Model model) {
        User currentUser = userRepository.findByEmail(resetEmailDTO.getEmail());

// If the user account does not exist, show error
        if (currentUser == null) {
            errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
            model.addAttribute("title", "Reset Account Password");
            return "user/reset2";
        }

// Creates a unique token string
        String token = UUID.randomUUID().toString();

// Connects the above created token to the user and saves it to the token db
        createPasswordResetTokenForUser(currentUser, token);
//        mailSender.send(constructResetTokenEmail(request.getLocale(), token, currentUser));

        return "index";
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(user, token);
        passwordTokenRepository.save(myToken);
    }

    private SimpleMailMessage constructResetTokenEmail(Locale locale, String token, User user) {
        String url = "http://localhost:8080/user/changePassword?token=" + token;
        String message = "This is a test email.";
        return constructEmail("Reset Password", message + " \r\n" + url, user);
    }

    private SimpleMailMessage constructEmail(String subject, String body,
                                             User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom("no.support@no.way");
        return email;
    }

// UPDATE PASSWORD START

// User -> Update password
    @GetMapping("update")
    public String showChangePasswordPage(Model model) {//, @RequestParam("token") String token) {
/*        String result = validatePasswordResetToken(token);
        if(result != null) {
            return "";
        } else {
            model.addAttribute("token", token);
            return "";
        } */
        model.addAttribute(new UpdatePasswordDTO());
        model.addAttribute("title", "Update Account Password");
        return "user/update";
    }

    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);

        return "";
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }

    @PostMapping("update")
    public String savePassword(@ModelAttribute @Valid UpdatePasswordDTO updatePasswordDTO, Errors errors, Model model) {

        String result = validatePasswordResetToken(updatePasswordDTO.getToken());

        if(result != null) {
            model.addAttribute("title", "Some title");
            return "";
        }

        PasswordResetToken userByToken = passwordTokenRepository.findByToken(updatePasswordDTO.getToken());
        User user = userRepository.findByEmail(userByToken.getUser().getEmail());

        if(user != null) {
            user.setPassword(updatePasswordDTO.getPasswordEntered());
            userRepository.save(user);
            return "";
        } else {
            return "";
        }
   }

// RESET END v2
}