package org.launchcode.closettracker.controllers;


import org.launchcode.closettracker.models.User;
import org.launchcode.closettracker.models.dto.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("")
    public String index (Model model){
        model.addAttribute("title", "My Closet");
        return "index";
    }

    @GetMapping("create")
    public String displayCreateAccountForm(Model model) {
        model.addAttribute(new UserDTO());
        model.addAttribute("title", "Create User Account");
        return "create";
    }
}
