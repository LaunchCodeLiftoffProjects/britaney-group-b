package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.User;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, Integer>  {
}
