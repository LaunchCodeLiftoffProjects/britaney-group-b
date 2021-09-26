package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.PasswordResetToken;
import org.launchcode.closettracker.models.User;
import org.springframework.data.repository.CrudRepository;

public interface PasswordTokenRepository extends CrudRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(User user);
    PasswordResetToken[] findAllByUser(User user);
    PasswordResetToken deleteByUser(User user);
}
