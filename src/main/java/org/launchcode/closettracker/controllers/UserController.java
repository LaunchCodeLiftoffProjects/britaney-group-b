package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.PasswordResetToken;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.*;
import org.launchcode.closettracker.repositories.ItemRepository;
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

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.launchcode.closettracker.controllers.SessionController.userSessionKey;
import static org.launchcode.closettracker.controllers.SessionController.getUserFromSession;

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private MailSender mailSender;

// Thymeleaf page template strings
    public static final String redirect = "redirect:";
    public static final String redirectIndex = "redirect:/index";

    public static final String goUserCreate = "user/create";

    private static final String goUserReset1st = "user/reset/reset";
    private static final String goUserReset2nd = "user/reset/reset-int";

    private static final String goUserUpdatePw = "user/reset/update";

    private static final String goEditInfo = "user/edit/info";
    private static final String goEditPassword = "user/edit/password";


    // A function to generate a random string of letters and numbers
    public String createRandomString(int strLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = strLength;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

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

// CREATE START
    //localhost:8080/create
    @GetMapping("create")
    public String displayCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create User Account");
        return goUserCreate;
    }

    @PostMapping("create")
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String createUser(@ModelAttribute @Valid UserDTO userDTO, Errors errors, HttpServletRequest request, Model model) throws IOException {
        try {
            if (errors.hasErrors()) {
                model.addAttribute("title", "Create User Account");
                /*model.addAttribute("errorMsg", "Bad data!");*/
                return goUserCreate;
            }

            User currentUser = userRepository.findByEmail(userDTO.getEmail());

            if (currentUser != null) {
                errors.rejectValue("email", "email.exists", "An account with this email address already exists");
                model.addAttribute("title", "Create User Account");
                return goUserCreate;
            }

            if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                errors.rejectValue("password", "passwords.nomatch", "Passwords do not match");
                model.addAttribute("pwdError", "Passwords do not match");
                model.addAttribute("title", "Create User Account");
                return goUserCreate;
            }

            User newUser = new User(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword(), false, true);
            userRepository.save(newUser);
            return redirectIndex;

        } catch (Exception ex) {
            if (ex.toString().contains("constraint")) {
                model.addAttribute("dbError", "Email exists. Try with new one!");
            } else {
                model.addAttribute("dbError", "Db Error");
            }
            return goUserCreate;
        }
    }
// CREATE END

// RESET START

// RECOVERY PART 1 - Reset password - enter email to generate token needed for step 2

// User --> Show email to reset form
    @GetMapping("reset/reset")
    public String displayStartResetForm(Model model, ResetEmailDTO resetEmailDTO) {
        model.addAttribute(new ResetEmailDTO());
        model.addAttribute("title", "Reset Account Password");
        return goUserReset1st;
    }

// User --> Process email to reset form
    @PostMapping("reset/reset")
    public String processStartResetForm(@ModelAttribute @Valid ResetEmailDTO resetEmailDTO, Errors errors,
                                        HttpServletRequest request, Model model, Model intModel) {

        User currentUser = userRepository.findByEmail(resetEmailDTO.getEmail());

    // If the user account does not exist, show error
        if (currentUser == null) {
            errors.rejectValue("email", "email.exists", "An account with this email address does not exist");
            model.addAttribute("title", "Reset Account Password");
            return goUserReset1st;
        }

    // Creates a unique token string
        String token = UUID.randomUUID().toString();
    // Delete any previous tokens for the user
        try {
            passwordTokenRepository.deleteById(currentUser.getId());
        }
        catch (Exception exception) {
            //
        }
    // Connects the above created token to the user and saves it to the token db
        createPasswordResetTokenForUser(currentUser, token);

    // While the User model does not persist the 'password' field, it is still required. So we need to...
        // 1) Reset the password hash by using a random password
            // Since 'password' is still a required object field, it needs a value so the user object will save
            // And the user shouldn't be able to use it to log in once the password reset process is started
            // So we use a random string to set the password value and replace the hash
        currentUser.setPassword(createRandomString(8));
        // 2) Ensure the user will have to update their password upon next login attempt, set the flag to true
        currentUser.setPasswordReset(true);
        // 3) Persist the finished User object
        userRepository.save(currentUser);
        User modifiedUser = currentUser;

    // Creates and sends an email to the user
    // For 'no outgoing email server error', add the Gmail SMTP outgoing email server credentials in the properties file
    // But don't forget to remove them before commit/push
    // The program will still run if no server is configured
        try {
            mailSender.send(constructResetTokenEmail(request.getLocale(), token, currentUser));
            intModel.addAttribute("message", "PASSWORD RESET AND EMAIL SENT");
            return goUserReset2nd;
        }
        catch (Exception exception) {
            if (exception.toString().contains("not accepted")) {
                errors.rejectValue("email", "server.notConfigured", "The password has been reset but no email was sent as there is no outgoing email server configured.");
                model.addAttribute("title", "Reset Account Password");
                intModel.addAttribute("token", token);
                intModel.addAttribute("message", "PASSWORD RESET NO EMAIL");
                return goUserReset2nd;
            } else {
                errors.rejectValue("email", "some.unknownError", "An unknown error occurred.");
                model.addAttribute("title", "Reset Account Password");
                return goUserReset1st;
            }
        }
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(user, token);
        passwordTokenRepository.save(myToken);
    }
// Creates the pieces needed for reset email construction
    private SimpleMailMessage constructResetTokenEmail(Locale locale, String token, User user) {
        String url = "http://localhost:8080/user/update?token=" + token;
        String message = "This automated message is to inform you that the password for your account at Closet Tracker has been reset." +
                "\n\nIf this was not you, then someone else has access to your account and that's not good." +
                "\n\nIf this was started by you then that's much better." +
                "\n\nYour unique reset token is: " + token + "\n\n" +
                "You can follow this link (http://localhost:8080/user/update) and enter the provided token manually to " +
                "reset your password or follow the link below to be taken to the same page with the token already filled in." +
                "\n\n(The prefill feature does not work yet. We sincerely apologize for the virtual inconvenience.)";
        return constructEmail("Your Closet Tracker password has been reset!", message + " \r\n\n" + url, user);
    }
// This function does the actual email construction
    private SimpleMailMessage constructEmail(String subject, String body,
                                             User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom("no.support@no.way");
        return email;
    }

// RECOVERY PART 2 - Update password - use token to choose a new password

    // User --> Show update password form
    @GetMapping("reset/update")
    public String showChooseNewPasswordForm(Model model, @RequestParam("token") String token) {
        boolean result = validatePasswordResetToken(token);
        if(result) {
            model.addAttribute("token", token);
        }
        model.addAttribute(new UpdatePasswordDTO());
        return goUserUpdatePw;
    }

    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        if(passToken != null) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }

// User --> Process update password form
    @PostMapping("reset/update")
    public String processChooseNewPasswordForm(@ModelAttribute @Valid UpdatePasswordDTO updatePasswordDTO, Errors errors,
                                            HttpServletRequest request, Model model) {

    // If the 1st password field is empty, display error message
        if (updatePasswordDTO.getPasswordEntered().isEmpty()) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordEntered", "passwordEntered.notMatch", "Passwords is required. Please try again.");
            return goUserUpdatePw;
        }

    // If the 2nd password field is empty, display error message
        if (updatePasswordDTO.getPasswordConfirm().isEmpty()) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordConfirm", "passwordConfirm.notMatch", "Password is required. Please try again.");
            return goUserUpdatePw;
        }

    // If both passwords do not match, display error message
        if (!updatePasswordDTO.getPasswordEntered().equals(updatePasswordDTO.getPasswordConfirm())) {
            model.addAttribute("passwordEntered", updatePasswordDTO.getPasswordEntered());
            model.addAttribute("passwordConfirm", updatePasswordDTO.getPasswordConfirm());
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("passwordConfirm", "passwordConfirm.notMatch", "Passwords do not match. Please try again.");
            return goUserUpdatePw;
        }

    // If reset token not found in db, display error message
        if(!validatePasswordResetToken(updatePasswordDTO.getToken())) {
            model.addAttribute("title", "Update Account Password");
            errors.rejectValue("token", "token.equals", "Token is not valid. Please try again.");
            return goUserUpdatePw;
        }
        int checkTokenSize = updatePasswordDTO.getToken().length();

        PasswordResetToken userByToken = passwordTokenRepository.findByToken(updatePasswordDTO.getToken());
        User user = userRepository.findByEmail(userByToken.getUser().getEmail());

        if(user != null) {
        // Updates the user object password with the entered one
        // This process creates a new password hash but does not persist the plain text password
            user.setPassword(updatePasswordDTO.getPasswordEntered());
        // Once password is successfully changed, set the reset flag to false to allow for normal login
            user.setPasswordReset(false);
        // Persists modified User object to db
            userRepository.save(user);
        // Once modified user object is saved, deletes the token from the token db
            passwordTokenRepository.deleteById(userByToken.getId());
        // Redirects user to login page
            model.addAttribute(new LoginFormDTO());
            model.addAttribute("title", "Welcome to Closet Tracker!");
            return redirectIndex;
        } else {
        // If user is not found, displays error message
            model.addAttribute("title", "Update Account Password");
            model.addAttribute("pwdError", "User not found. Please try again.");
            return goUserUpdatePw;
        }
    }

// RESET END

// EDIT ACCOUNT START

// User --> Show edit account info
    @GetMapping("edit/info")
    public String showEditAccountInfoForm(@ModelAttribute EditInfoDTO editInfoDTO,
                                          Errors errors, Model model, Model loginModel, HttpSession session) {
    // Get current user
        User currentUser = getUserFromSession(session);

    // If user object is null, redirect to login page
        if (currentUser == null) {
            loginModel.addAttribute("title", "Login");
            return redirectIndex;
        }

    // Set DTO fields with values from User db
        editInfoDTO.setUsername(currentUser.getUserName());
        editInfoDTO.setEmail(currentUser.getEmail());
        model.addAttribute(editInfoDTO);
        return goEditInfo;
    }

    public boolean checkIfUserEmailIsUnique(User currentUser, String changedEmail) {
        try {
            User editingUser = userRepository.findByEmail(changedEmail);
            int editingUserId = editingUser.id;
            int currentUserId = currentUser.id;
            if (editingUser == null || editingUserId == currentUserId) {
                return true;
            }
            else {
                return false;
            }
        } catch(Exception exception) {
            return false;
        }
    }

// User --> Process edit account info
    @PostMapping("edit/info")
    public String processEditAccountInfoForm(@ModelAttribute @Valid EditInfoDTO editInfoDTO, Errors errors,
                                             HttpServletRequest request, HttpSession session, Model model) {
/* Some cases to plan for:
    1) Since fields are prefilled with persisted info, when user hits Update compare the field values to the stored values, do nothing if same
    2) Since fields can be changed together or separately, check each as individual fields
    3) Username is not used for login so it can be whatever the user wants
    4) Email *IS* used for login, so it must be unique - check email against db, then check userid vs currentUser. show error if not match
*/
    // Get current user
        User currentUser = getUserFromSession(session);
        boolean isUsernameChanged = false;
        boolean isEmailChanged = false;
    // If the user account does not exist, redirect to login page as browser session has expired
        if (currentUser == null) {
            errors.rejectValue("email", "user.DoesNotExist", "User is not logged in or user does not exist.");
            return redirectIndex;
        }
    // If username equals the stored username, display a message saying
        if (editInfoDTO.getUsername().equals(currentUser.getUserName())) {
            model.addAttribute("message", "Info did not change so you're all good!");
        }
    // If names are different, change that field and set changed flag to true
        else {
            currentUser.setUserName(editInfoDTO.getUsername());
            isUsernameChanged = true;
        }
    // If email equals the stored username, display a message saying
        if (editInfoDTO.getEmail().equals(currentUser.getEmail())) {
            model.addAttribute("message", "Info did not change so you're all good!");
        }
    // Call function to check email from edit form
        else if(checkIfUserEmailIsUnique(currentUser, editInfoDTO.getEmail())) {
            currentUser.setEmail(editInfoDTO.getEmail());
            isEmailChanged = true;
        }
        else {
            errors.rejectValue("email", "email.NotUnique", "The email entered cannot be used. Please try again.");
        }

        if (isUsernameChanged || isEmailChanged) {

            userRepository.save(currentUser);
            if (isUsernameChanged && isEmailChanged) {
                model.addAttribute("message", "Username and Email have been updated");
            } else if (isUsernameChanged) {
                model.addAttribute("message", "Username has been updated");
            } else if (isEmailChanged) {
                model.addAttribute("message", "Email has been updated");
            }
        }
        model.addAttribute("username", editInfoDTO.getUsername());
        model.addAttribute("email", editInfoDTO.getEmail());
        model.addAttribute("title", "Edit Account Information");
        return goEditInfo;
    }

    // User --> Show edit password
    @GetMapping("edit/password")
    public String showEditPasswordForm(Model model) {
        model.addAttribute(new EditPasswordDTO());
        return goEditPassword;
    }

// EDIT ACCOUNT END

}
