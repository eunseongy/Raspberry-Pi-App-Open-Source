package com.example.dust_sensor_connection;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    ConnectivityManager connectman;
    ConnectivityManager cm;
    SensorManager sm;
    Sensor sensor;
    WifiManager wifiman;
    TextView tv;
    TextView tv2;
    TextView tv3;
    List sensorList;
    BluetoothAdapter blead;
    //로컬 장치 Bluetooth Adapter을 나타냄
    BluetoothLeScanner blescanner;
    //Bluetooth LE 장치에 대한 스캔 관련 작업을 수행하는 메서드를 제공
    Context context;
    int count;
    SEL sel;
    //SEL : Sensor Event Listener
    // ㄴ 센서 데이터를 sensorManager에게 알림

    //임시
    String wifidata = "";


    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    TextView textStatus;
    Button btnParied, btnSearch, btnSend;
    ListView listView;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    //11주차 ------------240525 ---------------
//    Gson gson = new GsonBuilder().setLenient().create();
//    private final Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl("http://203.255.81.72:10021/")
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build();

//    private BroadcastReceiver rssiReceiver;

    //----------------------------

    BluetoothAdapter ap1;
    //기본 로컬 불루투스 어댑터에 대한 핸들을 가져온다.
    //항상 기본 어댑터가 반환됨.

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},1000);
            return;
        }

        context = getApplicationContext();
        Log.e("test", String.valueOf(context));
        blead = BluetoothAdapter.getDefaultAdapter();

        String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        //데이터 전송 시 receiver 부분에 전송할 정보
        //기기 별로 구분이 되어야 함.
        //다음의 코드로 핸드폰의 고유 id를 얻어와서 receiver로 전송.



        blead.enable();

        blescanner = blead.getBluetoothLeScanner();
        //스캔 시작! onCreate에 있어서 실행과 동시에 스캔을 계속함
        blescanner.startScan(scanCallback);

        wifiman = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);


        //여기서부터 10주차 실습 수업자료 코드-----------
        tv = findViewById(R.id.tv);     //위치 데이터?
        tv2 = findViewById(R.id.tv2);   //와이파이
        tv3 = findViewById(R.id.tv3);   //블루투스 확인
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        /*센서 매니저
        * - 센서 서비스의 인스턴스 생성
        * - 센서 접근 및 나열, sensorEventListener 등록 및 취소 등의 method 제공*/
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //특정 센서의 인스턴스를 만들 수 있음
        sel = new SEL();

        String[] permission_list = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        //Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!btAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // variables
        textStatus = (TextView) findViewById(R.id.text_status);
        btnParied = (Button) findViewById(R.id.btn_paired);
        btnSend = (Button) findViewById(R.id.btn_send);
        listView = (ListView) findViewById(R.id.listview);

        // show paired devices
        btArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        listView.setOnItemClickListener(new myOnItemClickListener());

        count = 0;

        sm.registerListener(sel, sensor, SensorManager.SENSOR_DELAY_UI);
        sensorList = sm.getSensorList(Sensor.TYPE_ALL);

        for(int i= 0;i<sensorList.size(); i++){
            //예비
            tv.setText(tv.getText() + sensorList.get(i).toString() + "\n");
        }
        // 사용 가능한 센서 목록

        wifiman = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        registerReceiver(wifireceiver, new IntentFilter((WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)));
        registerReceiver(wifireceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        wifiman.startScan();

        List<ScanResult> scanresult = wifiman.getScanResults();



        for(int i = 0; i< scanresult.size(); i++){
            int level = scanresult.get(i).level;
            String BSSID=scanresult.get(i).BSSID;
            String SSID = scanresult.get(i).SSID;

            tv2.setText("level: "+level+"\t BSSID: "+BSSID+"\t SSID: "+SSID+"\n");
            System.out.println("scan");
        }

        connectman = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);

        if(connectman.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
            tv2.setText("와이파이 연결 됨\n");
        }
        else{
            tv2.setText("와이파이 연결 안 됨\n");
        }
        //---------와이파이 연결 확인 메서드-----------
        // ↓ 블루투스 연결 확인 ----------------------
        cm = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        //connectman이랑 똑같은데 이름만 cm임
        if(cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH).isConnected()){
            tv.setText("블루투스 연결 됨");
        }
        else{
            tv.setText("블루투스 연결 안 됨");
        }

        //11주차 ------------Location authentication

//        void start() throws InterruptedException{
//            Log.e("wifi", "Start");
//            tv.setText("");
//            count =0;
//            wifidata = "";
//            if(!wifiman.startScan()){
//                tv.setText("스캔 실패!\n");
//            }
//        }

    }

    //주변 BLE Scan
    ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result){
            super.onScanResult(callbackType, result);

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }

            String MACAdd = result.getDevice().getAddress();
            String name = result.getDevice().getName();
            double RSSI = result.getRssi();

            tv3.setText(tv3.getText()+"MAC: "+MACAdd + "\t name: "+name+"\t RSSI: "+RSSI+"\n");
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult){
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        if(grantResult[0] == PackageManager.PERMISSION_GRANTED);
    }

    public BroadcastReceiver wifireceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiman.startScan();
            @SuppressLint("MissingPermission") List<ScanResult> scanresult = wifiman.getScanResults();

            for(int i=0;i<scanresult.size();i++){
                int level = scanresult.get(i).level;
                String BSSID = scanresult.get(i).BSSID;
                String SSID = scanresult.get(i).SSID;

                tv2.setText(tv2.getText()+"level: "+level+"\t BSSID: "+BSSID+"\t SSID: "+SSID+"\n");
                System.out.println("scan");
            }
        }
    };


    @SuppressLint("MissingPermission")
    public void onClickButtonPaired(View view) {

        // Paired Device 버튼을 클릭하면 핸드폰과 pairing 되어 있는 기기 정보 출력

        btArrayAdapter.clear();
        if(deviceAddressArray!=null && !deviceAddressArray.isEmpty()){
            deviceAddressArray.clear();
        }
        pairedDevices = btAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            //there are apied devices. Get the name and address of each paired device.
            for(BluetoothDevice device : pairedDevices){
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); //Mac address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
    }


    // Send string "a"
    public void onClickButtonSend(View view) {
        //send a 버튼을 클릭하면 연결된 기기에 a라는 문자 전송
        if(connectedThread!=null){ connectedThread.write("a"); }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {
    // pairing 된 기기 목록에서 특정 기기 목록을 클릭하면 해당 기기와 connection 생성
        @SuppressLint("MissingPermission")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

            textStatus.setText("try...");

            final String name = btArrayAdapter.getItem(position); //get name
            final String address = deviceAddressArray.get(position); //get address
            boolean flag = true;

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            // create & connect socket
            try{
                btSocket = createBluetoothSocket(device);
            }catch (IOException e){
                textStatus.setText("connection failed!");
                e.printStackTrace();
            }

            try{
                btSocket.connect();
            }
            catch(IOException e2){
                try{
                    btSocket.close();
                }catch (IOException e){
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
            }
            // start bluetooth communication
            connectedThread = new ConnectedThread(btSocket);
            textStatus.setText("connected to" + name);
            connectedThread.start();
        }



        @SuppressLint("MissingPermission")
        private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
            // Bluetooth socket 생성
            // 일종의 bluetooth 통신을 위한 통신로
            try{
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
                return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
            } catch (Exception e){
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
            return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
        }
    }



    private class SEL implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent sensorEvent){
            //센서 값에 변화가 있을 때마다 호출됨
            if(count < 4){
                if(count == 0){
                    tv.setText("측정되는 데이터 종류의 수: " + sensorEvent.values.length+"\n");
                }
                for(int i=0;i<sensorEvent.values.length; i++){
                    float v = sensorEvent.values[i];
                    tv.setText(tv.getText()+Float.toString(v)+" ");
                }
                tv.setText(tv.getText()+"\n");
                count++;
            }
            else{
                sm.unregisterListener(sel);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }



}



