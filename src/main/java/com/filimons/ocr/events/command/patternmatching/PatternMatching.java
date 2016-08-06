package com.filimons.ocr.events.command.patternmatching;

public class PatternMatching {
    private Pattern[] patterns;

    public PatternMatching(Pattern... patterns) {
        this.patterns = patterns;
    }

    public Object matchFor(Object object, Object arg) {
        for (Pattern pattern : patterns)
            if (pattern.matches(object))
                return pattern.apply(arg);
        throw new IllegalArgumentException("cannot match " + object);
    }
}