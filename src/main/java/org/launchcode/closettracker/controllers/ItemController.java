package org.launchcode.closettracker.controllers;

import org.launchcode.closettracker.models.Color;
import org.launchcode.closettracker.models.FileUploadUtil;
import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.launchcode.closettracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.launchcode.closettracker.controllers.SessionController.userSessionKey;
import static org.launchcode.closettracker.controllers.SessionController.goRedirect;
import static org.launchcode.closettracker.controllers.SessionController.goRedirectIndex;

@Controller
@RequestMapping("items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchService searchService;

// Thymeleaf template page strings
    private static final String goItemCreate = "items/create-item";

    private static final String goItemCloset = "items/closet";

    private static final String goItemDetails = "items/details";

    private static final String goItemEdit = "items/edit";

    private static final String goItemDelete = "items/delete";

    private static final String goSearchResult = "/items/search_result";

    private static final String goRedirectItemDetails = "redirect:details?itemId=";

    public User getUserFromSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(userSessionKey);
        if (userId == null) {
            return null;
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return null;
        }

        return user.get();

    }

    // CREATE ITEM: Show form
    @GetMapping("create-item")
    public String displayCreateItemForm(Model model) {
        model.addAttribute(new Item());
        return goItemCreate;
    }

    // CREATE ITEM: Process form
    @PostMapping("create-item")
    public String processCreateItemForm(@ModelAttribute @Valid Item item, Errors errors,
                                        HttpSession session, Model model,
                                        @RequestParam("image") MultipartFile image) throws IOException {
        if(errors.hasErrors()) {
            // No error handling here?
            return goItemCreate;
        }

        String fileName = StringUtils.cleanPath(image.getOriginalFilename());
        item.setItemImage(fileName);
    // Gets user id from current session to find the User object
        User currentUser = getUserFromSession(session);
    // If user null, it should redirect user to login page to log in before allowing item creation
    // This is to catch the call to itemRepository before it throws the 500 error
        if(currentUser == null) {
            model.addAttribute("message", "Browser session has expired or is no longer valid. You must log in to create an item.");
            return goRedirectIndex;
        }
    // As user id is required to create items, sets the user object for the item so it can be created
        item.setUser(currentUser);
        itemRepository.save(item);
        String uploadDirectory = "item-photos/" + item.getId();
        FileUploadUtil.saveFile(uploadDirectory, fileName, image);
        return goRedirect;
    }

    // get current users username - in progress

    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName(Principal principal) {
        return principal.getName();
    }

    // Displays all items for logged in user in My Closet

    @GetMapping
    public String displayAllItems(Model objModel, Model model, Principal principal, HttpSession session) {
        User currentUser = getUserFromSession(session);
        model.addAttribute("title", "My Closet");
        objModel.addAttribute("items", itemRepository.findByUser(currentUser));
        return goItemCloset;
    }


    // View Item Details

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

        return goItemDetails;
    }

    // Edit Item Details
/*
    @PostMapping("view-by-user")
    public String recallItemsByUser(Model model, HttpSession session) {
        int userId = 1;
        User currentUser = getUserFromSession(session);
        User item = itemRepository.findByUserid(userId);

        return "item";
    }
*/
    @GetMapping("edit")
    public String displayEditItemDetailsForm(@RequestParam Integer itemId, Model model) {

        Optional<Item> itemToEdit = itemRepository.findById(itemId);


            Item item = itemToEdit.get();
            model.addAttribute(item);
            model.addAttribute("title", "Edit " + item.getItemName() + " Details");
            model.addAttribute("item", item);

        return goItemEdit;
    }

   @PostMapping("edit")
    public String processUpdateItemDetails(@ModelAttribute @Valid Item item, Errors errors, Integer itemId, Model model,
                                           String itemName, String type, Color color, String size, String[] season, @RequestParam(value = "image", required = false)
                                           MultipartFile image) throws IOException {

       if (errors.hasErrors()) {
           model.addAttribute("title", "Edit " + item.getItemName() + "Details");
           return goItemEdit;
       }


       model.addAttribute("title", item.getItemName() + " Details");
       model.addAttribute("item", item);

       Optional<Item> optionalItem = itemRepository.findById(itemId);
       Item itemToEdit = optionalItem.get();

       if (image.isEmpty()) {

           itemToEdit.setItemName(itemName);
           itemToEdit.setType(type);
           itemToEdit.setColor(color);
           itemToEdit.setSize(size);
           itemToEdit.setSeason(season);
           Item savedItem = itemRepository.save(itemToEdit);

           return goRedirectItemDetails + itemId;

       } else {

           String fileName = StringUtils.cleanPath(image.getOriginalFilename());
           itemToEdit.setItemImage(fileName);
           itemToEdit.setItemName(itemName);
           itemToEdit.setType(type);
           itemToEdit.setColor(color);
           itemToEdit.setSize(size);
           itemToEdit.setSeason(season);
           Item savedItem = itemRepository.save(itemToEdit);
           String uploadDirectory = "item-photos/" + savedItem.getId();
           //  FileUtils.deleteDirectory(new File("item-photos/" + savedItem.getId()));
           FileUploadUtil.saveFile(uploadDirectory, fileName, image);
       }
     //  Item savedItem = itemRepository.save(itemToEdit);

 /*      Files.delete(Path.of("item-photos/" + fileName));
       Files.deleteIfExists(Path.of("item-photos/" + savedItem.getId()));
       String uploadDirectory = "item-photos/" + savedItem.getId();
       FileUtils.deleteDirectory(new File("item-photos/" + savedItem.getId()));
       FileUploadUtil.saveFile(uploadDirectory, fileName, image);*/



       return goRedirectItemDetails + itemId;
   }

   // SEARCH ITEMS    items/search

   @GetMapping("/search")
   public String search(@Param("keyword") String keyword, Model model, Model objModel, HttpSession session){

       User currentUser = getUserFromSession(session);

       List<Item> searchResult = searchService.search(keyword, currentUser);
       objModel.addAttribute("items", itemRepository.findByUser(currentUser));

       if (searchResult.isEmpty()) {
           model.addAttribute("message","No matching items for '" + keyword + "' found");
           return goSearchResult;
       } else
       model.addAttribute("keyword", keyword);
       model.addAttribute("title", "Search results for " + keyword + "");
       model.addAttribute("searchResult", searchResult);
       return goSearchResult;
   }


    // DELETE ITEM(s): Show form
    @GetMapping("delete")
    public String displayDeleteItemForm(Model objModel, Model model, HttpSession session) {
        User currentUser = getUserFromSession(session);
        objModel.addAttribute("items", itemRepository.findByUser(currentUser));
        return goItemDelete;
    }

    // DELETE ITEM(s): Process form
    @PostMapping("delete")
    public String processDeleteItemsForm(@RequestParam(required = false) int[] itemIds) {

        if (itemIds != null) {
            for (int id : itemIds) {
                itemRepository.deleteById(id);
            }
        }

        return goRedirect;
    }


    @PostMapping("details")
    public String processDeleteOneItemForm(@RequestParam(value="itemId", required = false) int[] itemIds) {

        if (itemIds != null) {
            for (int id : itemIds) {
                itemRepository.deleteById(id);
                //need to delete photo from filesystem as well
                // FileUtils.deleteDirectory("item-photos/" + itemImage.getId());
            }
        }

        return goRedirect;
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
