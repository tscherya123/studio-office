package com.fetty.studiooffice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
public class WelcomeController {

    @GetMapping("/welcome")
    public String showForm() {
        return "welcome";
    }
}
