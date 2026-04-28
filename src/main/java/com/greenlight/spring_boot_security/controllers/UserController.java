package com.greenlight.spring_boot_security.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.greenlight.spring_boot_security.models.User;
import com.greenlight.spring_boot_security.service.UserService;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user")
    public String userPage(Principal principal, Model model) {
        Optional<User> authorisedUser = userService.findUserByName(principal.getName());
        if (authorisedUser.isPresent()) {
            model.addAttribute("authorisedUser", authorisedUser.get());
        }
        return "user_page";
    }

}