package com.filimons.ocr.events.photo;

import com.filimons.ocr.messages.*;
import com.filimons.ocr.ocrapi.*;
import com.filimons.ocr.telegram.*;
import com.google.common.cache.*;
import com.google.common.eventbus.*;
import com.google.gson.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class PhotoHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    final private OcrAPI ocrAPI;
    final private Bot bot;
    final private Executor ocrWorkers;
    final private Cache<Long, String> cache;
    final private Messages messages;

    public PhotoHandler(OcrAPI ocrAPI, Bot bot, Executor ocrWorkers, Cache<Long, String> cache, Messages messages) {
        this.ocrAPI = ocrAPI;
        this.bot = bot;
        this.ocrWorkers = ocrWorkers;
        this.cache = cache;
        this.messages = messages;
    }

    @Subscribe
    public void photo(PhotoEvent event) {
        CompletableFuture
                .supplyAsync(() -> bot.retrieveUrl(event.photoId), ocrWorkers)
                .thenApply((url) -> ocr(params(url, userLang(event.chat))))
                .thenAccept(result -> bot.chatAnswer(event.chat, result))
                .whenComplete((u, ex) -> {
                    if (ex != null) {
                        String error = String.format(messages.get("log.event.error"), new Gson().toJson(event), ex);
                        log.error(error);
                        bot.toAdmin(error);
                    }
                });
    }

    private String ocr(OcrAPI.Params params) {
        String ocrText = null;
        try {
            ocrText = ocrAPI.ocr(params).execute().body().string();
        } catch (IOException ex) {
            String error = String.format(messages.get("log.ocr.error"), new Gson().toJson(params), ex);
            log.error(error);
            bot.toAdmin(error);
            ocrText = messages.get("event.error");
        }
        return ocrText;
    }

    private OcrAPI.Params params(String url, Optional<String> lang) {
        return new OcrAPI.Params(
                url,
                new OcrAPI.EngineArgs(lang.orElse("eng"))
        );
    }

    private Optional<String> userLang(long chat) {
        return Optional.ofNullable(cache.getIfPresent(chat));
    }
}
