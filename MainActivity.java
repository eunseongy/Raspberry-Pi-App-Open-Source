package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;

import java.time.LocalDate;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    Button bt1;             //블루투스 스캔 버튼
    Button bt_write;        //데이터 쓰기 버튼
    Button bt_store;        //데이터 저장 버튼
    Button bt_show;         //데이터 출력 버튼
    TextView tv;
    TextView tv2;           //데이터 패킷 수집해서 출력.
    BluetoothAdapter ap1;
    EditText ed_rssi;
    EditText ed_pm1_0;
    EditText ed_pm25;
    EditText ed_pm10;
    Button packet_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ap1 = BluetoothAdapter.getDefaultAdapter();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        bt1 = (Button) findViewById(R.id.clickButton); //클릭 버튼 생성
        bt_write = (Button) findViewById(R.id.bt_write); //데이터 입력 버튼 생성
        bt_store = (Button) findViewById(R.id.bt_store); //데이터 저장 버튼 생성
        bt_show = (Button) findViewById(R.id.bt_show); //데이터 출력 버튼
        ed_rssi = (EditText) findViewById(R.id.ed_rssi);
        ed_pm1_0 = (EditText) findViewById(R.id.ed_pm1_0);
        ed_pm25 = (EditText) findViewById(R.id.ed_pm25);
        ed_pm10 = (EditText) findViewById(R.id.ed_pm10);
        packet_bt = (Button) findViewById(R.id.packet_bt);

        tv = (TextView) findViewById(R.id.tv);          //데이터 출력 textView
        tv2 = (TextView) findViewById(R.id.tv2);
        ArrayList<BLEdata_storage> dataList = new ArrayList<>();


        bt_write.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {        //데이터 입력 버튼 클릭 시 데이터 set 저장

                int rssi = Integer.valueOf(ed_rssi.getText().toString());
                int pm1_0 = Integer.valueOf(ed_pm1_0.getText().toString());
                int pm2_5 = Integer.valueOf(ed_pm25.getText().toString());
                int pm10 = Integer.valueOf(ed_pm10.getText().toString());


                BLEdata_storage data = new BLEdata_storage(rssi, pm1_0, pm2_5, pm10, System.currentTimeMillis());

                dataList.add(data);     //데이터 추가

            }
        });

        bt_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv");
                    if (!file.exists()) {
                        file.createNewFile();
                    }


                    FileWriter fw = new FileWriter(file.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);


                    for (int j = 0; j < dataList.size(); j++) {
                        bw.write(String.valueOf(dataList.get(j).get_rssi()));
                        bw.write("," + String.valueOf(dataList.get(j).get_p01()));
                        bw.write("," + String.valueOf(dataList.get(j).get_p25()));
                        bw.write("," + String.valueOf(dataList.get(j).get_p10()));
                        bw.write("," + String.valueOf(dataList.get(j).get_time()));

                        bw.newLine();

                    }

                    bw.close();
                    fw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                dataList.clear();
            }
        });

        bt_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tv.setText("");
                    String line;

                    BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/store_test.csv"));
                    //매개변수로 입력된 파일 내용 불러오기

                    while ((line = br.readLine()) != null) {
                        tv.setText(tv.getText() + line + "\n"); //한 줄 씩 데이터 읽어오기
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //postdata 클래스의 객체 생성
                ble ble = new ble();
                ble.BLEscan(ap1);
            }
        });
    }

    public class BLEdata_storage {       //데이터 관리 class 생성

        private int RSSI;
        private int p01;
        private int p25;
        private int p10;
        private long time;

        BLEdata_storage(int RSSI, int p01, int p25, int p10, long time) {
            this.RSSI = RSSI;
            this.p01 = p01;
            this.p25 = p25;
            this.p10 = p10;
            this.time = time;
        }       //생성자

        public int get_rssi() {
            return RSSI;
        }

        public int get_p01() {
            return p01;
        }

        public int get_p25() {
            return p25;
        }

        public int get_p10() {
            return p10;
        }

        public long get_time() {
            return time;
        }

    }

}

