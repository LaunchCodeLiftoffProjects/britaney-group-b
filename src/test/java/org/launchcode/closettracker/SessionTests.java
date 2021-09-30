package org.launchcode.closettracker;

import com.mysql.cj.AbstractQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.launchcode.closettracker.controllers.HomeController;
import org.launchcode.closettracker.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/* Session tests needed:
    1) Is a user session created correctly?
    2) Is the user's unique id stored in the browser session correctly?


*/





@SpringBootTest
public class SessionTests {

    HomeController homeController;

    protected HttpServletRequest request;

    private void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
    }

///* Trying to setup a test for the set and getUserFromSession functions

//*/
//    @Test
//    public void checkSetUserIdToZeroCorrectly() {
//        HttpSession session;
//        User testUser = new User();
//        testUser.setId(0);
//        int askldjlasdj = testUser.getId();
//        homeController.setUserInSession(session, testUser);
//        assertEquals("0", testUser.getId()); // homeController.getUserFromSession(session));
//    }

    @Test
    public void checkDisplayPhraseIsCreatedCorrectly() {
        User testUser = new User();
        assertEquals("Stevie's Closet", testUser.makeDisplayPhrase("Stevie"));
        assertEquals("Lucas' Closet", testUser.makeDisplayPhrase("Lucas"));
        assertEquals("Princess' Closet", testUser.makeDisplayPhrase("Princess"));
    }

    @Test
    public void checkNewUserObjectCreatedCorrectly() {
        User testUser = new User("Luke Tiberius Cakewalker", "luke@cakewalker.net", "asdasdasd", false,true);
    // username
        assertEquals("Luke Tiberius Cakewalker", testUser.getUserName());
    // email
        assertEquals("luke@cakewalker.net", testUser.getEmail());
    // password hash
        assertTrue(testUser.isEncodedPasswordEqualsInputPassword("asdasdasd"));
    // displayName
        assertEquals("Luke", testUser.getDisplayName());
    // displayPhrase
        assertEquals("Luke's Closet", testUser.getDisplayPhrase());
    // password reset flag
        assertFalse(testUser.isPasswordReset());
    // new user flag
        assertTrue(testUser.isNewUser());
    }

}