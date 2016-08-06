package com.filimons.ocr.ocrapi;

import okhttp3.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface OcrAPI {

    @POST("ocr")
    Call<ResponseBody> ocr(@Body Params params);

    class Params {
        String img_url;
        String engine;
        com.filimons.ocr.ocrapi.OcrAPI.EngineArgs engine_args;

        public Params(String img_url, com.filimons.ocr.ocrapi.OcrAPI.EngineArgs engine_args) {
            engine = "tesseract";
            this.img_url = img_url;
            this.engine_args = engine_args;
        }
    }

    class EngineArgs {
        String lang;

        public EngineArgs(String lang) {
            this.lang = lang;
        }
    }
}
