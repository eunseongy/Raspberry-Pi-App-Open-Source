package com.example.tts_test;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    Button start, stop;
    EditText et;
    FloatingActionButton fab; //그냥 버튼

    private tts tts;

    //protected TextToSpeech.OnInitListener onInitListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init, 리스너 생성
        TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    //tts = new tts(getApplicationContext(), this::onInit);
                    Log.e("TTS", "INIT SUCCESS");
                } else{
                    Log.e("TTS", "INIT FAILED");
                }

            }
        };
        //tts 객체 생성
        tts = new tts(this.getApplicationContext(), onInitListener);

        //textview, edittext
        tv =findViewById(R.id.tv);
        et = findViewById(R.id.et);
        //메시지버튼같은거 클릭
        //스타트 버튼 누르면 edittext에 쓴 글자를 textview에 쓰고 읽은
        start = findViewById(R.id.start);
        start.setOnClickListener(view -> {
            CharSequence Text = et.getText();
            tv.setText(Text);
            //tts.speak(Text, TextToSpeech.QUEUE_FLUSH, null, 'test');
            tts.setPitch(0.6F); //톤 높이
            tts.setSpeechRate(1.0f); //음성 속도
            tts.speak(Text, 0, null, "testUter");



        });

        //그냥 테스트용
        stop = findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText("stop!!");
            }
        });

        //그냥 텍스트뷰 지우기
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText("");

            }
        });
    }



    //ondestroy 필수
    @Override
    protected void onDestroy() {
        tts.shutdown();
        super.onDestroy();
    }
}