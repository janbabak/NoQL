package com.janbabak.noqlbackend.service.langChain;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

interface Assistant {

    String chat(@UserMessage String userMessage);

    String chat(List<ChatMessage> messages);
}
