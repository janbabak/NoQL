package com.janbabak.noqlbackend.service.langchain;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;

@FunctionalInterface
interface Assistant {

    String chat(List<ChatMessage> messages);
}
