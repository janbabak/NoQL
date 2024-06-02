package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.Settings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettingsController.class)
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Settings settings;

    @Test
    @DisplayName("Get settings")
    void getSettings() throws Exception {
        // when
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
}