package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
public class SettingsController {

    @Autowired // TODO: remove this annotation
    Settings settings;

    /**
     * @return application settings
     */
    @GetMapping
    private Settings getSettings() {
        return settings;
    }

}
