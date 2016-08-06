package com.filimons.ocr.pooling;

import com.codahale.metrics.*;
import com.filimons.ocr.events.command.*;
import com.filimons.ocr.events.inline.*;
import com.filimons.ocr.events.photo.*;
import com.filimons.ocr.events.text.*;
import com.filimons.ocr.telegram.*;
import com.google.common.eventbus.*;
import com.pengrad.telegrambot.model.*;

import java.util.*;
import java.util.function.*;

public class Gateway implements Parser {

    final private Bot bot;
    final private EventBus eventBus;
    final private Meter messagesMeter;

    public Gateway(Bot bot, EventBus eventBus, Meter messagesMeter) {
        this.bot = bot;
        this.eventBus = eventBus;
        this.messagesMeter = messagesMeter;
    }

    public void parseUpdates() {
        List<Update> updates = bot.updates();
        updates.stream().map(Update::message).filter(Objects::nonNull).forEach(this::parseAndFilter);
        updates.stream().map(Update::inlineQuery).filter(Objects::nonNull).map(InlineEvent::buildFromInline).forEach(eventBus::post);
    }

    private void parseAndFilter(Message message) {
        postInBusIfFilter(message, (m) -> m.photo() != null, PhotoEvent::buildFromPhoto);
        postInBusIfFilter(message, (m) -> m.document() != null && m.document().mimeType().equals("image/jpeg"), PhotoEvent::buildFromDocument);
        postInBusIfFilter(message, (m) -> (m.text() != null && !m.text().startsWith("/")), TextEvent::buildFromMessage);
        postInBusIfFilter(message, (m) -> (m.text() != null && m.text().startsWith("/")), CommandEvent::buildFromMessage);
    }

    private void postInBusIfFilter(Message message, Predicate<Message> filter, Function<Message, Object> builder) {
        if (filter.test(message)) {
            messagesMeter.mark();
            eventBus.post(builder.apply(message));
        }
    }
}
