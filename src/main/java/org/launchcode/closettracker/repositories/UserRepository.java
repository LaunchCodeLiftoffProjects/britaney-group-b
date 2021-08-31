package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByEmail(String email);
  //  User findByUsername(String username);
}

