package com.filimons.ocr;

import com.codahale.metrics.*;
import com.filimons.ocr.events.command.*;
import com.filimons.ocr.events.inline.*;
import com.filimons.ocr.events.photo.*;
import com.filimons.ocr.events.text.*;
import com.filimons.ocr.messages.*;
import com.filimons.ocr.ocrapi.*;
import com.filimons.ocr.pooling.*;
import com.filimons.ocr.telegram.*;
import com.google.common.cache.Cache;
import com.google.common.cache.*;
import com.google.common.eventbus.*;
import com.google.common.util.concurrent.*;
import com.pengrad.telegrambot.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.context.i18n.*;
import org.springframework.context.support.*;
import org.springframework.scheduling.annotation.*;
import retrofit2.*;
import retrofit2.converter.gson.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@EnableScheduling
@SpringBootApplication
public class BotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @Bean
    public TelegramBot telegramBot(@Value("${bot.token}") String token) {
        return TelegramBotAdapter.build(token);
    }

    @Bean
    public Bot bot(TelegramBot bot, @Value("${bot.admin}") long adminChat) {
        return new Telegram(bot, 0, adminChat);
    }

    @Bean
    public Parser parser(Bot bot, EventBus eventBus, Meter messagesMeter) {
        return new Gateway(bot, eventBus, messagesMeter);
    }

    @Bean
    public SchedulePooling schedulePooling(Parser parser) {
        return new SchedulePooling(parser);
    }

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public Retrofit retrofit(@Value("${ocr.url}") String url, OkHttpClient httpClient) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();
    }

    @Bean
    public OcrAPI ocrRestAPI(Retrofit retrofit) {
        return retrofit.create(OcrAPI.class);
    }

    @Bean
    public Cache<Long, String> cache(MetricRegistry metricRegistry) {
        Cache<Long, String> cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .recordStats()
                .build();
        metricRegistry.register("cache stats", (Gauge<String>) () -> cache.stats().toString());
        return cache;
    }

    @Bean(name = "languages")//todo: know how to inject maps
    public Map<String, String> languages() {
        return Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("eng", "English"),
                new AbstractMap.SimpleEntry<>("ara", "Arabic"),
                new AbstractMap.SimpleEntry<>("bel", "Belarusian"),//or belgian
                new AbstractMap.SimpleEntry<>("ben", "Bengali"),
                new AbstractMap.SimpleEntry<>("bul", "Bulgarian"),
                new AbstractMap.SimpleEntry<>("ces", "Czech"),
                new AbstractMap.SimpleEntry<>("dan", "Danish"),
                new AbstractMap.SimpleEntry<>("deu", "German"),
                new AbstractMap.SimpleEntry<>("ell", "Greek"),
                new AbstractMap.SimpleEntry<>("fin", "Finnish"),
                new AbstractMap.SimpleEntry<>("fra", "French"),
                new AbstractMap.SimpleEntry<>("heb", "Hebrew"),
                new AbstractMap.SimpleEntry<>("hin", "Hindi"),
                new AbstractMap.SimpleEntry<>("ind", "Indonesian"),
                new AbstractMap.SimpleEntry<>("isl", "Icelandic"),
                new AbstractMap.SimpleEntry<>("ita", "Italian"),
                new AbstractMap.SimpleEntry<>("jpn", "Japanese"),
                new AbstractMap.SimpleEntry<>("kor", "Korean"),
                new AbstractMap.SimpleEntry<>("nld", "Dutch"),
                new AbstractMap.SimpleEntry<>("nor", "Norwegian"),
                new AbstractMap.SimpleEntry<>("pol", "Polish"),
                new AbstractMap.SimpleEntry<>("por", "Portuguese"),
                new AbstractMap.SimpleEntry<>("ron", "Romanian"),
                new AbstractMap.SimpleEntry<>("rus", "Russian"),
                new AbstractMap.SimpleEntry<>("spa", "Spain"),
                new AbstractMap.SimpleEntry<>("swe", "Swedish"),
                new AbstractMap.SimpleEntry<>("tha", "Thai"),
                new AbstractMap.SimpleEntry<>("tur", "Turkish"),
                new AbstractMap.SimpleEntry<>("ukr", "Ukrainian"),
                new AbstractMap.SimpleEntry<>("vie", "Vietnamese"),
                new AbstractMap.SimpleEntry<>("chi-sim", "Chinese"),
                new AbstractMap.SimpleEntry<>("chi-tra", "Chinese")
        ).collect(
                Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new)));
    }


    @Bean
    public PhotoHandler photoHandler(OcrAPI ocrAPI, Bot bot, Executor ocrWorkers, Cache<Long, String> userLanguage, Messages messages) {
        return new PhotoHandler(ocrAPI, bot, ocrWorkers, userLanguage, messages);
    }

    @Bean
    public Executor ocrWorkers() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Ocr-Workers-%d")
                .setDaemon(true)
                .build();
        return Executors.newFixedThreadPool(5, threadFactory);
    }

    @Bean
    public CommandHandler commandHandler(Bot bot, Terminal terminal) {
        return new CommandHandler(bot, terminal);
    }

    @Bean
    public Terminal terminal(Cache<Long, String> userLanguage, Messages messages) {
        return new Terminal(userLanguage, languages(), messages);
    }

    @Bean
    public TextHandler textHandler(Bot bot, Messages messages) {
        return new TextHandler(bot, messages);
    }

    @Bean
    public InlineHandler inlineHandler(Bot bot, Messages messages) {
        return new InlineHandler(bot, messages);
    }

    @Bean
    public EventBus eventBus(PhotoHandler photoHandler, CommandHandler commandHandler,
                             TextHandler textHandler, InlineHandler inlineHandler) {
        EventBus bus = new EventBus();
        bus.register(photoHandler);
        bus.register(commandHandler);
        bus.register(textHandler);
        bus.register(inlineHandler);
        return bus;
    }

    @Bean
    public Messages messages(ReloadableResourceBundleMessageSource messageSource) {
        return new MessagesByLocale(messageSource, LocaleContextHolder.getLocale());
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:locale/messages");
        return messageSource;
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public Meter messagesMeter(MetricRegistry metricRegistry) {
        return metricRegistry.meter("processed messages");
    }

    @Bean
    public Slf4jReporter metricsReporter(MetricRegistry metricRegistry) {
        Slf4jReporter reporter = Slf4jReporter
                .forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.HOURS)
                .convertDurationsTo(TimeUnit.MINUTES)
                .build();
        reporter.start(1, TimeUnit.HOURS);
        return reporter;
    }

    @Bean
    public CsvReporter csvMetricsReporter(MetricRegistry metricRegistry) {
        CsvReporter reporter = CsvReporter.
                forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.DAYS)
                .convertDurationsTo(TimeUnit.HOURS)
                .build(new File("./"));
        reporter.start(1, TimeUnit.DAYS);
        return reporter;
    }
}
