package com.filimons.ocr.events.photo;

import com.pengrad.telegrambot.model.*;

public class PhotoEvent {
    final public String photoId;
    final public long chat;

    public PhotoEvent(String fileId, long chatId) {
        this.photoId = fileId;
        this.chat = chatId;
    }

    public static PhotoEvent buildFromDocument(Message message) {
        return new PhotoEvent(message.document().fileId(), message.chat().id());
    }

    public static PhotoEvent buildFromPhoto(Message message) {
        return new PhotoEvent(maxSizePhoto(message).fileId(), message.chat().id());
    }

    private static PhotoSize maxSizePhoto(Message message) {
        PhotoSize[] photos = message.photo();
        return photos[photos.length - 1];
    }
}
