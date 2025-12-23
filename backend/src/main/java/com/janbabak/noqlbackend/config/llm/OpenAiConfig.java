package com.janbabak.noqlbackend.config.llm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.external-services.open-ai-api")
public class OpenAiConfig {

    private String apiKey;
    private List<String> supportedModels;
}