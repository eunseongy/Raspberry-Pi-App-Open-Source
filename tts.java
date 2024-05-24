package com.example.tts_test;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;
import android.view.View;
public class tts extends TextToSpeech {
    private int lang;

    public tts(Context context, OnInitListener listener) {
        super(context, listener);
        this.setPitch(1.0f);
        lang = this.setLanguage(Locale.KOREA);
        if(lang == this.LANG_NOT_SUPPORTED || lang == this.LANG_MISSING_DATA){
            Log.e("TTS", "LANGUAGE NOT SUPPORTED");
        } else {
            Log.e("TTS", "LANGUAGE SUPPORTED");

        }



    }

    @Override
    public void shutdown() {
        super.stop();
        super.shutdown();
    }

}
