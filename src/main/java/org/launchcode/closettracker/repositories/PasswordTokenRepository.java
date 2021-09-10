package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

public interface PasswordTokenRepository extends CrudRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
}

