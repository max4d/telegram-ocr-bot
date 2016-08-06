package com.filimons.ocr.events.command;

import com.filimons.ocr.telegram.*;
import com.google.common.eventbus.*;

import java.util.*;

public class CommandHandler {

    final private Bot bot;
    final private Terminal terminal;

    public CommandHandler(Bot bot, Terminal terminal) {
        this.bot = bot;
        this.terminal = terminal;
    }

    @Subscribe
    public void handle(CommandEvent event) {
        bot.chatAnswer(event.chat, prepareAnswer(event));
    }

    private String prepareAnswer(CommandEvent event) {
        Scanner scanner = new Scanner(event.text).useDelimiter("\\s");
        String command = scanner.next();
        String arg = scanner.hasNext() ? scanner.next() : null;
        return terminal.answerOn(new Command(
                command,
                Optional.ofNullable(arg),
                event.chat));
    }
}
