package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Integer> {
    List<Item> findByUser(User user);
}
