package com.janbabak.noqlbackend.model.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageWithResponseRequest {

    @NotBlank private String message; // users query
    @NotBlank private String response; // LLM response JSON
}
