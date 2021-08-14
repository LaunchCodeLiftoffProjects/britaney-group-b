package org.launchcode.closettracker.controllers;


import org.launchcode.closettracker.models.User;
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
    public String create (Model model){
        model.addAttribute("title","Create User");
        model.addAttribute(new User());
        return  "create";
    }
}
