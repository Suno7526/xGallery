package com.example.xGallery.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.xGallery.domain.XUserProfile;
import com.example.xGallery.service.XUserProfileService;

@Controller
public class HomeController {

    private final XUserProfileService xUserProfileService;

    public HomeController(XUserProfileService xUserProfileService) {
        this.xUserProfileService = xUserProfileService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        String username = principal.getName();
        List<XUserProfile> xUsers = xUserProfileService.findByUsername(username);

        model.addAttribute("username", username);
        model.addAttribute("xUsers", xUsers);
        return "home";
    }

    @PostMapping("/home/x-users")
    public String addXUser(@RequestParam String screenName, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            xUserProfileService.register(principal.getName(), screenName);
            return "redirect:/home";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/home";
        }
    }
}
