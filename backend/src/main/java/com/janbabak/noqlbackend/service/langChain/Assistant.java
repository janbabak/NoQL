package com.janbabak.noqlbackend.service.langChain;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface Assistant {

    @SystemMessage("Your role is to assist users in generating database queries and data visualizations based on their requests.")
    String chat(@UserMessage String userMessage);
}
