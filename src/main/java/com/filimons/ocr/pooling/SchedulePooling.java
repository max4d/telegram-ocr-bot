package com.filimons.ocr.pooling;

import org.springframework.scheduling.annotation.*;

public class SchedulePooling {

    final private Parser parser;

    public SchedulePooling(Parser parser) {
        this.parser = parser;
    }

    @Scheduled(fixedDelayString = "${schedule.milliseconds}")
    public void checkMessages() {
        parser.parseUpdates();
    }
}
