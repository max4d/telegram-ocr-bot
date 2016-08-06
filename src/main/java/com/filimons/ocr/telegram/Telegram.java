package com.filimons.ocr.telegram;

import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;

import java.util.*;

public class Telegram implements Bot {

    private int offset;
    private long adminChat;
    final private TelegramBot bot;

    public Telegram(TelegramBot bot, int offset, long adminChat) {
        this.offset = offset;
        this.bot = bot;
        this.adminChat = adminChat;
    }

    public List<Update> updates() {
        List<Update> updates = bot.execute(new GetUpdates().offset(offset)).updates();
        calculateOffset(updates);
        return updates;
    }

    public String retrieveUrl(String fileId) {
        GetFileResponse getFileResponse = bot.execute(new GetFile(fileId));
        File file = getFileResponse.file();
        return bot.getFullFilePath(file);
    }

    public void chatAnswer(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    public void inlineAnswer(String inlineId, String text) {
        bot.execute(
                new AnswerInlineQuery(inlineId, new InlineQueryResultArticle("inline", text, text)));
    }

    public void toAdmin(String text) {
        bot.execute(new SendMessage(adminChat, text));
    }

    private void calculateOffset(List<Update> updates) {
        if (!updates.isEmpty()) {
            int max = updates.stream()
                    .max((u1, u2) -> Integer.compare(u1.updateId(), u2.updateId()))
                    .get().updateId();
            if (max >= offset) {
                offset = max + 1;
            }
        }
    }
}
