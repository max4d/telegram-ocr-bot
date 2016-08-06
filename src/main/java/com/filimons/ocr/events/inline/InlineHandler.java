package com.filimons.ocr.events.inline;

import com.filimons.ocr.messages.*;
import com.filimons.ocr.telegram.*;
import com.google.common.eventbus.*;

public class InlineHandler {

    final private Bot bot;
    final private Messages messages;

    public InlineHandler(Bot bot, Messages messages) {
        this.bot = bot;
        this.messages = messages;
    }

    @Subscribe
    public void handle(InlineEvent event) {
        bot.inlineAnswer(event.inlineId, messages.get("event.inline.answer"));
    }
}
