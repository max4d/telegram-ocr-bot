package com.filimons.ocr.ocrapi;

import com.filimons.ocr.*;
import com.filimons.ocr.events.photo.*;
import com.filimons.ocr.telegram.*;
import com.google.gson.*;
import org.junit.*;
import org.junit.runner.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.*;
import org.springframework.test.context.junit4.*;

import java.io.*;
import java.util.concurrent.*;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BotApplication.class)
public class OcrIntegrationTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Bot bot;

    @Autowired
    OcrAPI ocrAPI;

    @Test
    public void ocrRuText() throws Exception {
        OcrAPI.Params params = new OcrAPI.Params(
                "http://s017.radikal.ru/i436/1111/f9/80ca1044e4a3.jpg",
                new OcrAPI.EngineArgs("rus")
        );
        String ocrText = ocrAPI.ocr(params).execute().body().string();
        System.out.println(ocrText);
    }

    @Test
    public void ocrEngText() throws Exception {
        OcrAPI.Params params = new OcrAPI.Params(
                "http://s017.radikal.ru/i433/1111/fc/a04dabb3c65f.jpg",
                new OcrAPI.EngineArgs("eng")
        );
        String ocrText = ocrAPI.ocr(params).execute().body().string();
        System.out.println(ocrText);
    }

    @Test
    public void photo() throws Exception {
        PhotoEvent photo = new PhotoEvent("AgADAgADuKcxG9tOCwAB_JYizXMFYE-TFEgNAAQitjVZ_T5a6-ynAAIC", 741083);
        CompletableFuture
                .supplyAsync(() -> bot.retrieveUrl(photo.photoId))
                .thenApply((url) -> ocr(params(url)))
                .thenAccept(result -> bot.chatAnswer(photo.chat, result))
                .whenComplete((u, ex) -> {
                    if (ex != null) {
                        log.error("photoEvent event is not processed: " + new Gson().toJson(photo));
                    }
                });
        TimeUnit.SECONDS.sleep(20);
    }

    private String ocr(OcrAPI.Params params) {
        String ocrText = null;
        try {
            ocrText = ocrAPI.ocr(params).execute().body().string();
            log.debug("ocr params: " + new Gson().toJson(params) + ". ocr result: " + ocrText);
        } catch (IOException e) {
            log.error("ocr completed with error - " + new Gson().toJson(params));
            ocrText = "ocr completed with error";
        }
        return ocrText;
    }

    private OcrAPI.Params params(String url) {
        return new OcrAPI.Params(
                url,
                new OcrAPI.EngineArgs("rus")
        );
    }
}