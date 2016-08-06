package com.filimons.ocr.telegram;

import com.pengrad.telegrambot.model.*;

import java.util.*;

public interface Bot {

    List<Update> updates();

    String retrieveUrl(String fileId);

    void chatAnswer(long chatId, String text);

    void inlineAnswer(String inlineId, String text);

    void toAdmin(String text);
}
