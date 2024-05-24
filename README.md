# Raspberry-Pi-App-Open-Source

## tts 기본 구현
__이 코드에서 동작__

edittext에 글자를 넣은 후 start 버튼을 누르면 textview에 입력된 글자가 출력되며 해당 문장을 읽음(기본 음성)
stop버튼은 "stop!!" 을 텍스트뷰에 넣는 기능, start버튼 활용하면 돼서 미구현
메시지같은 버튼은 초기 네비게이션 화면에서 다른 요소들은 지워지고 남은 흔적, 단순히 textview 지우는 용도로 활용 

- manifesto
  
  targetAPI = "31"
  
   ``` <action android:name="android.intent.action.TTS_SERVICE" /> ``` 추가

- main_activity

   버튼 같은 것들은 그냥 시각적인 것들이라 중요하지 않음
  - import 
  ```import android.speech.tts.TextToSpeech; ```
  - oncreate
    
    OninitListener 생성,  구조 상 리스너만 생성 => *중요, **onInit** 생성 필수(리스너 안에 있음)
    
        -> 한꺼번에 할거면 tts의 생성자 내용을 여기에 넣거나 적절히 조합(하면 될듯)
    
    TextToSpeech(context, listener)
        -> 객체 생성 시 위의 리스너를 컨텍스트랑 넣으면 생성자에서 알아서 하는 구조

    onDestroy()
    
         - 종료 시 tts 객체 종료해야함
    
         - 앱 종료 시 삭제되도록 ondestroy에 코드 삽입
    
         - tts의 shutdown 함수를 오버라이드 해서 tts.shutdown()을 onDestroy에 넣음
  
- tts
  생성자에 언어 설정함, 톤 높이(pitch), 음성 속도(speech rate)등 설정 가능(할듯)
  
  _생성자_ : TTS 엔진 필요 시 context, listener, engine 으로 변경

  shutdown()

      stop, shutdown 오버라이드 해서 한번에 사용
  
  PITCH, SPEECH_RATE(실수형)
  
  UteranceId -> 그냥 고유한 값이라곤 하는데 잘 모르겠음 문서에도 

