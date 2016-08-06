package com.filimons.ocr.events.text;

import com.pengrad.telegrambot.model.*;

public class TextEvent {
    final long chat;

    public TextEvent(long chat) {
        this.chat = chat;
    }

    public static TextEvent buildFromMessage(Message message) {
        return new TextEvent(message.chat().id());
    }
}
