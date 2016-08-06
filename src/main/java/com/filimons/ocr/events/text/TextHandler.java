package com.filimons.ocr.events.text;

import com.filimons.ocr.messages.*;
import com.filimons.ocr.telegram.*;
import com.google.common.eventbus.*;
import org.slf4j.*;

public class TextHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    final private Bot bot;
    final private Messages messages;

    public TextHandler(Bot bot, Messages messages) {
        this.bot = bot;
        this.messages = messages;
    }

    @Subscribe
    public void handle(TextEvent event) {
        bot.chatAnswer(event.chat, messages.get("event.text.answer"));
    }
}
