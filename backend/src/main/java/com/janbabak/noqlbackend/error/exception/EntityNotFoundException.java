package com.janbabak.noqlbackend.error.exception;

import java.util.UUID;

public class EntityNotFoundException extends Exception {

    @SuppressWarnings("unused")
    public EntityNotFoundException() {
        super();
    }

    @SuppressWarnings("unused")
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Entity entity, UUID id) {
        super(entity.label + " of id: \"" + id + "\" not found.");
    }

    public EntityNotFoundException(Entity entity, String id) {
        super(entity.label + " of identifier: \"" + id + "\" not found.");
    }

    /**
     * Entity that has not been found.
     */
    public enum Entity {
        DATABASE("Database"),
        CHAT("Chat"),
        CUSTOM_MODEL("Custom Model"),
        USER("User"),
        MESSAGE("Message");

        public final String label;

        Entity(String label) {
            this.label = label;
        }
    }
}
