package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(JwtService.class)
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Settings settingsMock;

    @Test
    @DisplayName("Get settings with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void getSettings() throws Exception {
        when(settingsMock.getMaxPageSize()).thenReturn(50);
        when(settingsMock.getDefaultPageSize()).thenReturn(10);
        when(settingsMock.getTranslationRetries()).thenReturn(3);
        when(settingsMock.getPlotServiceContainerName()).thenReturn("plot-service-dev-stack");
        when(settingsMock.getDefaultUserQueryLimit()).thenReturn(10);

        // then
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json("""
                        {
                             "maxPageSize": 50,
                             "defaultPageSize": 10,
                             "translationRetries": 3,
                             "plotServiceContainerName": "plot-service-dev-stack",
                             "defaultUserQueryLimit": 10
                        }""", true));
    }

    @Test
    @DisplayName("Get settings with anonymous user")
    @WithAnonymousUser
    void getSettingAnonymousUser() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get settings with USER role")
    @WithMockUser(roles = "USER")
    void getSettingUser() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isForbidden());
    }
}