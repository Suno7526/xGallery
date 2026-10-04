package com.example.xGallery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.xGallery.service.UserAccountService;

@Controller
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("user") SignupRequest request, RedirectAttributes redirectAttributes) {
        try {
            userAccountService.register(request.username(), request.password(), request.email());
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/signup";
        }
    }

    public record SignupRequest(String username, String password, String email) {
        public SignupRequest() {
            this("", "", "");
        }
    }
}
