package org.launchcode.closettracker.controllers;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.launchcode.closettracker.models.Color;
import org.launchcode.closettracker.models.FileUploadUtil;
import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Optional;

import static org.launchcode.closettracker.controllers.HomeController.userSessionKey;

@Controller
@RequestMapping("items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    public User getUserFromSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(userSessionKey);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        }
        else {
            return null;
        }
    }

    // CREATE ITEM: Show form
    @GetMapping("create-item")
    public String displayCreateItemForm(Model model) {
        model.addAttribute(new Item());
        model.addAttribute("title", "Add Item");
        return "items/create-item";
    }

    // CREATE ITEM: Process form
    @PostMapping("create-item")
    public String processCreateItemForm(@ModelAttribute @Valid Item newItem, Errors errors, Model model,
                                        @RequestParam("image") MultipartFile multipartFile, HttpSession session,
                                        HttpServletRequest request) throws IOException {
        if(errors.hasErrors()) {
            model.addAttribute("title", "Add Item");
            return "items/create-item";
        }

        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        newItem.setItemImage(fileName);

    // Retrieve userId stored in session key "user"
        User currentUser = getUserFromSession(session);

    // If retrieval is successful, extract user object and return
        if(currentUser != null) {
            newItem.setUser(currentUser);
            itemRepository.save(newItem);
            return "redirect:";
        }
        else {
            model.addAttribute("title", "Create Item");
            return "items/create-item";
        }

    }

    //get current users username - in progress

    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName(Principal principal) {
        return principal.getName();
    }

    //Displays all items in closet

    @GetMapping
    public String displayAllItems(Model objModel, Model model, Principal principal)
    {
        model.addAttribute("title", "My Closet");
        objModel.addAttribute("items", itemRepository.findAll());
        return "items/closet";
    }


    // We are making View Item Details and Edit Item Details the same page
    @GetMapping("details")
    public String displayItemDetails(@RequestParam Integer itemId, Model model) {

        Optional<Item> result = itemRepository.findById(itemId);

        if (result.isEmpty()) {
            model.addAttribute("title", "Invalid Item ID: " + itemId);
        } else {
            Item item = result.get();
            model.addAttribute("title", item.getItemName() + " Details");
            model.addAttribute("item", item);
        }

        return "items/details";
    }

    @GetMapping("edit")
    public String editItemDetails(@RequestParam Integer itemId, Model model) {

        Optional<Item> itemToEdit = itemRepository.findById(itemId);

            Item item = itemToEdit.get();
            model.addAttribute("item", itemToEdit);
            model.addAttribute("title", "Edit " + item.getItemName() + " Details");
            model.addAttribute("item", item);

        return "items/edit";
    }

    @PostMapping("edit")
    public String updateItemDetails(@RequestParam @Valid Integer itemId, Model model)
                                   // String itemName, String type, Color color, String size) // {
    {
   /*    Optional<Item> optionalItem = itemRepository.findById(itemId);
      Item itemToEdit = optionalItem.get();

     itemToEdit.setItemName(itemName);
     itemToEdit.setType(type);
     itemToEdit.setColor(color);
     itemToEdit.setSize(size);
     itemToEdit.setSeason(season);*/

        return "items/details";
    }

    // DELETE ITEM(s): Show form
    @GetMapping("delete")
    public String displayDeleteItemForm(Model model) {
        model.addAttribute("title", "Delete Items");
        model.addAttribute("items", itemRepository.findAll());
        return "items/delete";
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

  /*  @GetMapping("/display/image/{id}")
    @ResponseBody
    public void showProductImage ( @PathVariable("id") int id,
                                   HttpServletResponse response) throws IOException {
        response.setContentType("image/jpg"); // Or whatever format we want to use

        Optional<Item> imageGallery = itemRepository.findById(id);

        InputStream is = new ByteArrayInputStream(imageGallery.get().getItemImage());
        IOUtils.copy(is, response.getOutputStream());

        //Files.write(Paths.get("resources/image/" + imageGallery.get().getName() + "." + "jpg"), imageGallery.get().getPic());
    }*/

}
