package com.janbabak.noqlbackend.model.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatResponse {
    private String databaseQuery;
    private Boolean generatePlot;
    private String pythonCode;
}
