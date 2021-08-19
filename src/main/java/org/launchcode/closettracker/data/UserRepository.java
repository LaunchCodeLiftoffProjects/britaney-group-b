package org.launchcode.closettracker.data;

import org.launchcode.closettracker.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {

    User findByUsername(String username);

}