package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.Settings;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Settings resource
 */
@RestController
@RequestMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SettingsController {

    private final Settings settings;

    /**
     * @return application settings
     */
    @GetMapping
    private Settings getSettings() {
        return settings;
    }
}
