package com.example.xGallery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example.xGallery.controller.HomeController;
import com.example.xGallery.service.XUserProfileService;

class HomeControllerTest {

    @Test
    void rootShouldRedirectToLogin() throws Exception {
        XUserProfileService xUserProfileService = mock(XUserProfileService.class);
        when(xUserProfileService.findByUsername("alice")).thenReturn(List.of());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HomeController(xUserProfileService)).build();

        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void homePageShouldExposeAuthenticatedUser() {
        XUserProfileService xUserProfileService = mock(XUserProfileService.class);
        when(xUserProfileService.findByUsername("alice")).thenReturn(List.of());

        HomeController homeController = new HomeController(xUserProfileService);
        Model model = new ExtendedModelMap();
        Principal principal = () -> "alice";

        String viewName = homeController.home(model, principal);

        assertThat(viewName).isEqualTo("home");
        assertThat(model.getAttribute("username")).isEqualTo("alice");
    }
}
