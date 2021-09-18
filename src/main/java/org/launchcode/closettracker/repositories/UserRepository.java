package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.User;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Integer> {
    User findByEmail(String email);
<<<<<<< HEAD
=======
   // User getUserName(String username);
>>>>>>> 35704606d2f543f5108d431733e8c2a81410c14e
}

