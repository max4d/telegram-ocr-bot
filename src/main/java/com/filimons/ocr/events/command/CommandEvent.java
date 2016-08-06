package com.filimons.ocr.events.command;

import com.pengrad.telegrambot.model.*;

public class CommandEvent {
    final public String text;
    final public long chat;

    public CommandEvent(String text, long chat) {
        this.text = text;
        this.chat = chat;
    }

    public static CommandEvent buildFromMessage(Message message) {
        return new CommandEvent(message.text(), message.chat().id());
    }
}
