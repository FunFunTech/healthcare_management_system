package com.healthcare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.service.UserService;

@Controller
public class AuthController {

    @Autowired private UserService userService;

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String password,
                           @RequestParam String role,
                           RedirectAttributes ra) {
        if (userService.emailExists(email)) {
            ra.addFlashAttribute("error", "Email already registered!");
            return "redirect:/register";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setRole(Role.valueOf(role.toUpperCase()));

        userService.registerUser(user);
        ra.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }
}
