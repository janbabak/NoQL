package com.janbabak.noqlbackend.model.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatResponse {
    private String translatedQuery;
    private Boolean plot;
    private List<String> columnsToPlot;
}
