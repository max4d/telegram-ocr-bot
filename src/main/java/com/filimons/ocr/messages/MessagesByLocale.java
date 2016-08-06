package com.filimons.ocr.messages;

import org.springframework.context.*;

import java.util.*;

public class MessagesByLocale implements Messages {

    final private MessageSource messageSource;
    final private Locale locale;

    public MessagesByLocale(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public String get(String id) {
        return messageSource.getMessage(id, null, locale);
    }
}
