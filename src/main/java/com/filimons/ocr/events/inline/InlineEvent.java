package com.filimons.ocr.events.inline;

import com.pengrad.telegrambot.model.*;

public class InlineEvent {
    final String inlineId;

    public InlineEvent(String inlineId) {
        this.inlineId = inlineId;
    }

    public static InlineEvent buildFromInline(InlineQuery query) {
        return new InlineEvent(query.id());
    }
}
