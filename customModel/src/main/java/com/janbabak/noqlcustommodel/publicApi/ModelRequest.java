package com.janbabak.noqlcustommodel.publicApi;

import jakarta.annotation.Nullable;

import java.util.List;

public record ModelRequest(
        @Nullable String model,
        List<LlmMessage> messages
) {}
