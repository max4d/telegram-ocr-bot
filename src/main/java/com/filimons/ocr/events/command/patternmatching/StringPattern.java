package com.filimons.ocr.events.command.patternmatching;

import java.util.function.*;

public class StringPattern implements Pattern {
    private final String pattern;
    private final Function function;

    public StringPattern(String pattern, Function function) {
        this.pattern = pattern;
        this.function = function;
    }

    @Override
    public boolean matches(Object value) {
        return pattern.equals(value);
    }

    @Override
    public Object apply(Object value) {
        return function.apply(value);
    }
}