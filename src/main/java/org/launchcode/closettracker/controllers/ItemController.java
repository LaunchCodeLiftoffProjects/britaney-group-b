package org.launchcode.closettracker.controllers;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.launchcode.closettracker.models.Color;
import org.launchcode.closettracker.models.FileUploadUtil;
import org.launchcode.closettracker.models.Item;
import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.UserDTO;
import org.launchcode.closettracker.repositories.ItemRepository;
import org.launchcode.closettracker.repositories.UserRepository;
import org.launchcode.closettracker.controllers.HomeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*import static org.launchcode.closettracker.controllers.HomeController.goRedirect;*/

@Controller
@RequestMapping("items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HomeController homeController;

    @Autowired
    private SearchService searchService;

// CREATE ITEM: Show form
    @GetMapping("create-item")
    public String displayCreateItemForm(Model model) {
        model.addAttribute(new Item());
        return "items/create-item";
    }

// CREATE ITEM: Process form
    @PostMapping("create-item")
    public String processCreateItemForm(@ModelAttribute @Valid Item item, Errors errors,
                                        HttpSession session, Model model,
                                        @RequestParam("image") MultipartFile image) throws IOException {
        if(errors.hasErrors()) {
            model.addAttribute("title", "Add Item");
            return "items/create-item";
        }

        String fileName = StringUtils.cleanPath(image.getOriginalFilename());
        item.setItemImage(fileName);
    // Gets user id from current session to find the User object
        User currentUser = homeController.getUserFromSession(session);
    // If user null, it should redirect user to login page to log in before allowing item creation
    // This is to catch the call to itemRepository before it throws the 500 error
        if(currentUser == null) {
            //
        }
    // As user id is required to create items, sets the user object for the item so it can be created
        item.setUser(currentUser);
        itemRepository.save(item);
        String uploadDirectory = "item-photos/" + item.getId();
        FileUploadUtil.saveFile(uploadDirectory, fileName, image);
        return "redirect:";
    }


    // Displays all items for logged in user in My Closet

    @GetMapping
    public String displayAllItems(Model objModel, Model model, HttpSession session)
    {
        User currentUser = homeController.getUserFromSession(session);
        model.addAttribute("title", homeController.getPhraseFromSession(session));
        model.addAttribute("phrase", homeController.getPhraseFromSession(session));
        objModel.addAttribute("items", currentUser.getItems());
        return "items/closet";
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

        return "items/details";
    }


    @GetMapping("edit")
    public String displayEditItemDetailsForm(@RequestParam Integer itemId, Model model) {

        Optional<Item> itemToEdit = itemRepository.findById(itemId);


            Item item = itemToEdit.get();
            model.addAttribute(item);
            model.addAttribute("title", "Edit " + item.getItemName() + " Details");
            model.addAttribute("item", item);

        return "items/edit";
    }

   @PostMapping("edit")
    public String processUpdateItemDetails(@ModelAttribute @Valid Item item, Errors errors, Integer itemId, Model model,
                                           String itemName, String type, Color color, String size, String[] season, @RequestParam(value = "image", required = false)
                                           MultipartFile image) throws IOException {

       if (errors.hasErrors()) {
           model.addAttribute("title", "Edit " + item.getItemName() + "Details");
           return "items/edit";
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

           return "redirect:details?itemId=" + itemId;

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



       return "redirect:details?itemId=" + itemId;
   }

   // SEARCH ITEMS    items/search

    @GetMapping("/search")
    public String search(@Param("keyword") String keyword, Model model, Model objModel, HttpSession session) throws SQLException {

        /*String dbURL = "jdbc:mysql://localhost:3306/closet_tracker";
        String username = "closet_tracker";
        String password = "closet_tracker";

        User currentUser = homeController.getUserFromSession(session);

        List<Item> thisUser = itemRepository.findByUser(currentUser);

        try {
            Connection connection = DriverManager.getConnection(dbURL, username, password);
            if (connection !=null) {
                System.out.println("Connected to the database");

                String sql = "SELECT * FROM item WHERE user_id= ? "
                        + "AND MATCH (item_name, type) "
                        + "AGAINST (?)";

                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(String.valueOf(thisUser)));
                ps.setString(2, keyword);

                ResultSet result = ps.executeQuery();

                while (result.next()) {
                    result.getInt("color");
                    result.getString("item_image");
                    result.getString("item_name");
                    result.getBlob("season");
                    result.getString("size");
                    result.getString("type");
                }

                connection.close();

                model.addAttribute("keyword", keyword);
                model.addAttribute("title", "Search results for " + keyword + "");
                model.addAttribute("searchResult", result);

            }
        } catch(SQLException exception) {
            exception.printStackTrace();

        }*/

        List<Item> searchResult = searchService.search(keyword);

        if (searchResult.isEmpty()) {
            model.addAttribute("message","No matching items for '" + keyword + "' found");
            return "/items/search_result";
        } else
            model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search results for " + keyword + "");
        model.addAttribute("searchResult", searchResult);
        return "/items/search_result";
    }


    // DELETE ITEM(s): Show form
    @GetMapping("delete")
    public String displayDeleteItemForm(Model model, HttpSession session) {
        User currentUser = homeController.getUserFromSession(session);
        model.addAttribute("title", "Delete Items");
        model.addAttribute("items", currentUser.getItems());
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


    @PostMapping("details")
    public String processDeleteOneItemForm(@RequestParam(value="itemId", required = false) int[] itemIds) {

        if (itemIds != null) {
            for (int id : itemIds) {
                itemRepository.deleteById(id);
                //need to delete photo from filesystem as well
                // FileUtils.deleteDirectory("item-photos/" + itemImage.getId());
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
