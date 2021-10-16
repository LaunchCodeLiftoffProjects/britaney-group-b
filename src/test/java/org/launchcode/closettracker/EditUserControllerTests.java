package org.launchcode.closettracker;

import org.launchcode.closettracker.controllers.EditUserController;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.UserRepository;

import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.launchcode.closettracker.models.User.passwordFixForSaveEditInfo;

@SpringBootTest
class EditUserControllerTests {

	private EditUserController editUserController;

	private UserRepository userRepository;

	@Test
	void checkPasswordFixWorksCorrectly() {
		try {
			User testUser = userRepository.findByEmail("bob@smith.org");
			if (testUser == null) {
				//
			}
			String testUserHash = testUser.getPwHash();
			testUser.setPassword("kahsdjkhiquwiuhfiufjk");
			passwordFixForSaveEditInfo(testUser);
			boolean doHashesMatch = testUserHash.equals(testUser.getPwHash());
			assertEquals(testUserHash, testUser.getPwHash());
		}
		catch (Exception exception) {
			assertEquals("chicken", "turkey");
		}
	}

}
