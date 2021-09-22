package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.Item;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Integer>  {

    @Query(value = "SELECT * FROM item WHERE "
    + "MATCH (item_name, type) "
    + "AGAINST (?1)",
    nativeQuery = true)
    public List<Item> search(String keyword);

}
