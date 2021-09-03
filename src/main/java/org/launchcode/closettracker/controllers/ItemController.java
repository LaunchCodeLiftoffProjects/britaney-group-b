package org.launchcode.closettracker.controllers;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.launchcode.closettracker.models.FileUploadUtil;
import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.dto.UserDTO;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Controller
@RequestMapping("items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    // CREATE ITEM: Show form
    @GetMapping("create-item")
    public String displayCreateItemForm(Model model) {
            model.addAttribute(new Item());
            model.addAttribute("title", "Create User Account");
            return "items/create-item";
    }

    // CREATE ITEM: Process form
    @PostMapping("create-item")
    public String processCreateItemForm(@ModelAttribute @Valid Item newItem,
                                         Errors errors, Model model, @RequestParam("image") MultipartFile multipartFile) throws IOException {
        if(errors.hasErrors()) {
            model.addAttribute("title", "Create Item");
            return "items/create-item";
        }

        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        newItem.setItemImage(fileName);

        Item savedItem = itemRepository.save(newItem);
        String uploadDirectory = "item-photos/" + savedItem.getId();
        FileUploadUtil.saveFile(uploadDirectory, fileName, multipartFile);
        return "redirect:";
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

        return "items/detail";
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
        return "items/detail";
    }

    @GetMapping
    public String displayAllItems(Model objModel)
    {
        objModel.addAttribute("items", itemRepository.findAll());
        return "items/closet";
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
