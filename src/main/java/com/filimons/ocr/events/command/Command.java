package com.filimons.ocr.events.command;

import java.util.*;

public class Command {
    final String name;
    final Optional<String> arg;
    final long chat;

    public Command(String name, Optional<String> arg, long chat) {
        this.name = name;
        this.arg = arg;
        this.chat = chat;
    }
}
