package com.janbabak.noqlbackend.model.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageWithResponseRequest {

    private String message;
    private String response;
}
