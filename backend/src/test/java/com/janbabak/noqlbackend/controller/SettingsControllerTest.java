package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettingsController.class)
@Import(JwtService.class)
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Settings settings;

    @Test
    @DisplayName("Get settings with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void getSettings() throws Exception {
        when(settings.getMaxPageSize()).thenReturn(50);
        when(settings.getDefaultPageSize()).thenReturn(10);
        when(settings.getTranslationRetries()).thenReturn(3);

        // then
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                             "maxPageSize": 50,
                             "defaultPageSize": 10,
                             "translationRetries": 3
                        }"""));
    }

    @Test
    @DisplayName("Get settings with anonymous user")
    @WithAnonymousUser
    void getSettingAnonymousUser() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isUnauthorized());
    }
}