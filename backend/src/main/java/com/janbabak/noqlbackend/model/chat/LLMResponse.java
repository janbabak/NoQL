package com.janbabak.noqlbackend.model.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LLMResponse {
    private String databaseQuery;
    private Boolean generatePlot;
    private String pythonCode;
}
