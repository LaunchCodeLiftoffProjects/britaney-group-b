//package org.launchcode.closettracker;
//
//import org.launchcode.closettracker.*;
//import org.junit.jupiter.api.Test;
//import org.launchcode.closettracker.repositories.UserRepository;
//import org.launchcode.closettracker.controllers.UserController;
//import org.launchcode.closettracker.models.User;
//
//import java.io.IOException;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
///**
// * Created by LaunchCode
// */
//public class ResetPasswordEmail {
//
//    private UserRepository userRepository;
//    /*
//    * Check application.properties for the correct db connection data
//    * */
//    @Test
//    public void checkEmail () throws IOException {
//
//        User user = userRepository.findByEmail("bob@smith.org");
//        UserController.createPasswordResetTokenForUser(user, token);
//
//    }
//
//    /*
//    * Check build.gradle for the required database dependencies
//    * */
//    @Test
//    public void testDbGradleDependencies () throws IOException {
//        String gradleFileContents = getFileContents("build.gradle");
//
//        Pattern jpaPattern = Pattern.compile("org.springframework.boot:spring-boot-starter-data-jpa");
//        Matcher jpaMatcher = jpaPattern.matcher(gradleFileContents);
//        boolean jpaFound = jpaMatcher.find();
//        assertTrue(jpaFound, "JPA dependency not found or is incorrect");
//
//        Pattern mysqlPattern = Pattern.compile("mysql:mysql-connector-java");
//        Matcher mysqlMatcher = mysqlPattern.matcher(gradleFileContents);
//        boolean mysqlFound = mysqlMatcher.find();
//        assertTrue(mysqlFound, "MySQL dependency not found or is incorrect");
//
//    }
//
//}
