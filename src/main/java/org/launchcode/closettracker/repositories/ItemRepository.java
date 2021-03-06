package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Integer> {

    @Query(value = "SELECT * FROM item WHERE "
            + "MATCH (item_name, type) "
            + "AGAINST (?1)",
            nativeQuery = true)
    public List<Item> search(String keyword);

    List<Item> findByUser(User user);

}

// Query will return items for specific user if specific user_id is given. How to get value in currentUser?
/*
      @Query(value = "SELECT * FROM item WHERE user_id = ? AND "
            + "MATCH (item_name, type) "
            + "AGAINST (?1)",
            nativeQuery = true)
    public List<Item> search(String keyword);*/


//original query returns items from all users

/*    @Query(value = "SELECT * FROM item WHERE "
            + "MATCH (item_name, type) "
            + "AGAINST (?1)",
            nativeQuery = true)
    public List<Item> search(String keyword);*/
