package com.filimons.ocr.events.command;

import com.filimons.ocr.events.command.patternmatching.*;
import com.filimons.ocr.messages.*;
import com.google.common.cache.*;

import java.util.*;
import java.util.function.*;

import static com.filimons.ocr.events.command.patternmatching.OtherwisePattern.*;

public class Terminal {

    final private Cache<Long, String> cache;
    final private Map<String, String> languages;
    final private PatternMatching pm;
    final private Messages messages;

    public Terminal(Cache<Long, String> cache,
                    Map<String, String> languages,
                    Messages messages) {
        this.cache = cache;
        this.languages = languages;
        this.messages = messages;
        pm = new PatternMatching(
                inCaseOf("/start", x -> messages.get("event.command.start")),
                inCaseOf("/help", x -> messages.get("event.command.help")),
                inCaseOf("/settings", this::settings),
                inCaseOf("/lang", this::language),
                inCaseOf("/list", this::list),
                otherwise(x -> messages.get("event.command.unknown"))
        );
    }

    public String answerOn(Command command) {
        return (String) pm.matchFor(command.name, command);
    }

    private String settings(Command command) {
        String lang = languages.getOrDefault(cache.getIfPresent(command.chat), "English");
        return String.format(messages.get("event.command.settings"), lang);
    }

    private String language(Command command) {
        if (command.arg.isPresent()) {
            return setLanguage(command);
        } else {
            return showLanguageInfo(command);
        }
    }

    private String list(Command command) {
        StringBuilder result = new StringBuilder(messages.get("event.command.list"));
        languages.forEach((k, v) -> result.append(String.format("%n %s - %s", k, v)));
        return result.toString();
    }

    private static Pattern inCaseOf(String pattern, Function<Command, String> function) {
        return new StringPattern(pattern, function);
    }

    private String showLanguageInfo(Command command) {
        String lang = languages.getOrDefault(cache.getIfPresent(command.chat), "English");
        return String.format(messages.get("event.command.language"), lang);
    }

    private String setLanguage(Command command) {
        if (languages.keySet().contains(command.arg.get())) {
            cache.put(command.chat, command.arg.get());
            return String.format(messages.get("event.command.language.result"), languages.get(command.arg.get()));
        } else {
            return messages.get("event.command.wrong.arg");
        }
    }
}
