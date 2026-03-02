package com.healthcare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/contact")
    public String contactSubmit(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String message,
                                 RedirectAttributes ra) {
        ra.addFlashAttribute("success", "Your message has been sent successfully!");
        return "redirect:/#contact";
    }
}
