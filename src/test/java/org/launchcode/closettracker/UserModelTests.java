package org.launchcode.closettracker;

import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import org.launchcode.closettracker.models.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserModelTests {

    @Test
    public void checkDisplayNameIsCreatedCorrectly() {
        User testUser = new User();
        assertEquals("Stevie", testUser.makeDisplayName("Stevie Nicks"));
    }

    @Test
    public void checkDisplayPhraseIsCreatedCorrectly() {
        User testUser = new User();
        assertEquals("Stevie's Closet", testUser.makeDisplayPhrase("Stevie"));
        assertEquals("Lucas' Closet", testUser.makeDisplayPhrase("Lucas"));
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