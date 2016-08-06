package com.filimons.ocr.events.command.patternmatching;

public interface Pattern {
    boolean matches(Object value);

    Object apply(Object value);
}
