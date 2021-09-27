package org.launchcode.closettracker.controllers;


import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Service
public class SearchService {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> search(String keyword) {
        return itemRepository.search(keyword);

    }


}
