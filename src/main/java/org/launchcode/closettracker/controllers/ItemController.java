package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.dto.UserDTO;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Optional;

public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    // CREATE ITEM: Show form
    @GetMapping("/items/create")
    public String displayCreateItemForm(Model model) {
            model.addAttribute(new Item());
            model.addAttribute("title", "Create User Account");
            return "/items/create";
    }

    // CREATE ITEM: Process form
    @PostMapping("/items/create")
    public String processCreateItemForm(@ModelAttribute @Valid Item newItem,
                                         Errors errors, Model model) {
        if(errors.hasErrors()) {
            model.addAttribute("title", "Create Item");
            return "create";
        }

        itemRepository.save(newItem);
        return "redirect:";
    }

    // DELETE ITEM(s): Show form
    @GetMapping("delete")
    public String displayDeleteItemForm(Model model) {
        model.addAttribute("title", "Delete Items");
        model.addAttribute("items", itemRepository.findAll());
        return "delete";
    }

    // DELETE ITEM(s): Process form
    @PostMapping("delete")
    public String processDeleteItemsForm(@RequestParam(required = false) int[] itemIds) {

        if (itemIds != null) {
            for (int id : itemIds) {
                itemRepository.deleteById(id);
            }
        }

        return "redirect:";
    }

    // We are making View Item Details and Edit Item Details the same page
    @GetMapping("detail")
    public String displayItemDetails(@RequestParam Integer itemId, Model model) {

        Optional<Item> result = itemRepository.findById(itemId);

        if (result.isEmpty()) {
            model.addAttribute("title", "Invalid Item ID: " + itemId);
        } else {
            Item item = result.get();
            model.addAttribute("title", item.getItemName() + " Details");
            model.addAttribute("item", item);
        }

        return "detail";
    }

    @PostMapping("detail")
    public String updateItemDetails(@RequestParam Integer itemId, Model model) {

        Optional<Item> result = itemRepository.findById(itemId);

        if (result.isEmpty()) {
            model.addAttribute("title", "Invalid Item ID: " + itemId);
        } else {
            Item item = result.get();
            model.addAttribute("title", item.getItemName() + " Details");
            model.addAttribute("item", item);
            itemRepository.save(item);
        }
        return "detail";
    }

}
