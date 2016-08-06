package com.filimons.ocr.events.command.patternmatching;

import java.util.function.*;

public class OtherwisePattern implements Pattern {
    private final Function<Object, Object> function;

    public OtherwisePattern(Function<Object, Object> function) {
        this.function = function;
    }

    @Override
    public boolean matches(Object value) {
        return true;
    }

    @Override
    public Object apply(Object value) {
        return function.apply(value);
    }

    public static Pattern otherwise(Function<Object, Object> function) {
        return new OtherwisePattern(function);
    }
}