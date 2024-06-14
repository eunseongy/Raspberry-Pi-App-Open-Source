package com.example.dust_sensor_connection;


import static android.os.SystemClock.sleep;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
    TextView tv4;
    EditText editTextNumber;
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
    Button btnParied, btnSearch, btnSend, btnAdv;
    ListView listView;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread;

    String loc_key;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},1000);
            return;
        }

        context = getApplicationContext();
        blead = BluetoothAdapter.getDefaultAdapter();

        blead.enable();

        blescanner = blead.getBluetoothLeScanner();
        //스캔 시작! onCreate에 있어서 실행과 동시에 스캔을 계속함
        blescanner.startScan(scanCallback);

        wifiman = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);


        //여기서부터 10주차 실습 수업자료 코드-----------
        tv = findViewById(R.id.tv);     //위치 데이터?
        tv2 = findViewById(R.id.tv2);   //와이파이
        tv3 = findViewById(R.id.tv3);   //블루투스 확인
        tv4 = findViewById(R.id.tv4);
        editTextNumber = findViewById(R.id.editTextNumber);
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
        btnAdv = (Button) findViewById(R.id.btn_adv);
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

        // Register Wi-Fi receiver
        // 스캔 성공시에만 데이터를 서버로 보내 현재 위치에 맞는 인증 key값을 받아오도록 구현
        // 한번에 현재 위치가 맞게 나오지 않을 수도 있음
        // 코드 상으로 scan이 성공 했을 때만 데이터를 서버로 보내는지 확실히 확인할 것
        btnAdv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                BroadcastReceiver rssiReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        boolean success = arg1.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                        if (success) {
                            scanSuccess();
                        } else {
                            tv.setText("스캔 실패 !");
                        }
                    }

                    private void scanSuccess() {
                        @SuppressLint("MissingPermission") List<ScanResult> scanresult = wifiman.getScanResults();
                        for (int i=0;i<scanresult.size();i++) {
                            int RSSI = scanresult.get(i).level;
                            String BSSID = scanresult.get(i).BSSID;
                            wifidata += (BSSID + "!" + String.valueOf(RSSI) + "/");
                        }

                        comm_data service = retrofit.create(comm_data.class);
//                 Log.e("testtest", wifidata);

                        Call<String> call = null;
                        call = service.location(wifidata);

                        call.enqueue(new Callback<String>() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
//                         Log.e("test loc", response.body().toString());
                                //수정예정 : 임시로 5-2로 함
                                loc_key = response.body().toString();
                                /*loc_key = "5-3";*/
                                tv4.setText("localization key: "+response.body().toString());
                                wifidata = "";
                                blescanner.stopScan(scanCallback);

                                blead.startLeScan(scanCallback_le);
                            }
                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Log.e("test_err", "1234");
                            }
                        });
                    }};
                registerReceiver(rssiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            }
        });
        /* registerReceiver는 안드로이드에서 브로드캐스트 리시버를 등록하는 데 사용되는 메서드.
         * 브로드캐스트리시버 예시 : 네트워크 상태 변경, 배터리 상태 변경, 문자 메시지 수신 등
         * registerReceiver 메서드를 통해 이벤트에 대해 리시버를 등록할 수 있음
         * 이벤트가 발생하면 브로드캐스트리시버의 onReceive메서드가 호출된다.
         * 여기선 SCAN_RESULTS_AVAILABLE_ACTION가 스캔 성공했으면 이벤트 발생이라고 지정*/
        wifiman = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        registerReceiver(wifireceiver, new IntentFilter((WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)));
        registerReceiver(wifireceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        if(!wifiman.startScan()){
            //startscan 함수의 return값을 받아와서 wifi 정보 스캔이 성공했는지 확인
            tv2.setText("스캔실패");
        }

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
    }

    Gson gson = new GsonBuilder().setLenient().create();
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://203.255.81.72:10021/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();



    //주변 BLE Scan
    ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result){
            super.onScanResult(callbackType, result);

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }

            // ppt에 있는 거 ------------
            String MACAdd = result.getDevice().getAddress();
            String name = result.getDevice().getName();
            double RSSI = result.getRssi();
            //  -------------------
            tv3.setText(tv3.getText()+"MAC: "+MACAdd + "\t name: "+name+"\t RSSI: "+RSSI+"\n");

        }
    };

    private final BluetoothAdapter.LeScanCallback scanCallback_le = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String MacAdd = device.getAddress();
            String data = byteArrayToHex(scanRecord);
            String timeOTP = "";
            String sensingTime = "";
            String sensorData = "";
            String sensor = "";
            int flag = 0;
            int s_flag =0;



            // dust sensor ---------------------------------
            if(MacAdd.equals("D8:3A:DD:42:AC:7F")||MacAdd.equals("D8:3A:DD:42:AC:64")||MacAdd.equals("B8:27:EB:DA:F2:5B")||MacAdd.equals("B8:27:EB:0C:F3:83")) {
                flag= 1;
                s_flag = 1;

                Log.e("test", data);
                sensor="1jo";
            }
            if(MacAdd.equals("D8:3A:DD:79:8F:97")||MacAdd.equals("D8:3A:DD:79:8F:B9")||MacAdd.equals("D8:3A:DD:79:8F:54")||MacAdd.equals("D8:3A:DD:79:8F:80")) {
                flag= 1;
                s_flag = 1;

                Log.e("test", data);
                sensor="2jo";
            }
            if(MacAdd.equals("D8:3A:DD:79:8E:D9")||MacAdd.equals("D8:3A:DD:42:AC:9A")||MacAdd.equals("D8:3A:DD:42:A8:FB")||MacAdd.equals("D8:3A:DD:79:8E:9B")) {
                flag= 1;
                s_flag = 1;

                Log.e("test", data);
                sensor="3jo";
            }
            if(MacAdd.equals("D8:3A:DD:78:A7:1A")||MacAdd.equals("D8:3A:DD:79:8E:BF")||MacAdd.equals("D8:3A:DD:79:8E:92")||MacAdd.equals("D8:3A:DD:79:8F:59")) {
                flag= 1;
                s_flag = 1;

                Log.e("test", data);
                sensor="4jo";
            }
            if(MacAdd.equals("B8:27:EB:D3:40:06")||MacAdd.equals("B8:27:EB:E4:D0:FC")||MacAdd.equals("B8:27:EB:47:8D:50")||MacAdd.equals("D8:3A:DD:78:A7:1A")) {
                Log.e("test", data);
                flag = 1;
                s_flag = 1;
                sensor = "5jo";
            }
            // air sensor --------------------------
            if(MacAdd.equals("D8:3A:DD:C1:89:2E")||MacAdd.equals("D8:3A:DD:C1:88:DD")||MacAdd.equals("D8:3A:DD:C1:89:1E")||MacAdd.equals("D8:3A:DD:C1:88:99")){
                flag = 1;
                s_flag=2;
                Log.e("test", data);
                sensor = "1jo";
            }if(MacAdd.equals("D8:3A:DD:C1:89:70")||MacAdd.equals("D8:3A:DD:C1:88:FE")||MacAdd.equals("D8:3A:DD:C1:89:79")||MacAdd.equals("D8:3A:DD:C1:89:C7")){
                flag = 1;
                s_flag=2;
                Log.e("test", data);
                sensor = "2jo";
            }if(MacAdd.equals("D8:3A:DD:C1:88:E8")||MacAdd.equals("D8:3A:DD:C1:89:5B")||MacAdd.equals("D8:3A:DD:C1:88:BD")||MacAdd.equals("D8:3A:DD:C1:88:D7")){
                flag = 1;
                s_flag=2;
                Log.e("test", data);
                sensor = "3jo";
            }
            if(MacAdd.equals("D8:3A:DD:C1:88:AD")||MacAdd.equals("D8:3A:DD:C1:89:64")||MacAdd.equals("D8:3A:DD:C1:88:62")||MacAdd.equals("D8:3A:DD:C1:88::C8")){
                flag = 1;
                s_flag=2;
                Log.e("test", data);
                sensor = "4jo";
            }
            if(MacAdd.equals("D8:3A:DD:C1:88:9B")||MacAdd.equals("D8:3A:DD:C1:89:07")||MacAdd.equals("D8:3A:DD:C1:88:95")||MacAdd.equals("D8:3A:DD:C1:89:87")){
                Log.e("test", data);

                flag = 1;
                s_flag=2;
                sensor = "5jo";
            }
            //-------------------------------------------

            if(flag == 1) { // 센서 맥주소 맞으면 실행
                String[] hexArray = data.split(" ");
                timeOTP =  findData(hexArray, 1);
                sensingTime = findData(hexArray, 2);

                sleep(1001);    //1초에 한 번 데이터 전송


                Log.e("test", "sensor: "+ sensor);
                Log.e("test", "mac: "+ MacAdd);
                Log.e("test", "otp: "+timeOTP);
                Log.e("test", "sensingTime: "+sensingTime);



//                ap1.stopLeScan(scanCallback_le); //BLE Scan 중지


                comm_data service = retrofit.create(comm_data.class);


                String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                //데이터 전송 시 receiver 부분에 전송할 정보
                //기기 별로 구분이 되어야 함.
                //다음의 코드로 핸드폰의 고유 id를 얻어와서 receiver로 전송.
                Log.e("test","id: "+ id);
                Log.e("test", "localization key: "+loc_key);




                Call<String> call = null;
                if(s_flag == 1){
                    sensorData = findData(hexArray, 3);
                    Log.e("test_sensing", "sensorData: "+ sensorData);

                    call = service.sensing(sensor, "advertising", MacAdd, id, sensingTime, timeOTP, loc_key, sensorData);
                }
                else if(s_flag == 2){
                    sensorData = findData(hexArray, 4);
                    Log.e("test_air", "sensorData: "+ sensorData);

                    call = service.air_string(sensor, "advertising", MacAdd, id, sensingTime, timeOTP, loc_key, sensorData);

                }
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Log.e("test", response.body().toString());
                        Log.e("test", "전송 성공");
                        //응답을 처리하는 코드
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("err", "1234123");
                        //실패 시 처리하는 코드
                    }
                });
            }
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
            //there are paired devices. Get the name and address of each paired device.
            for(BluetoothDevice device : pairedDevices){
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); //Mac address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
    }


    // Send string "a"
    String message = "";


    public void onClickButtonSend(View view) {
        //send a 버튼을 클릭하면 연결된 기기에 a라는 문자 전송

        if(connectedThread!=null){
            message = editTextNumber.getText().toString();
            connectedThread.setMessageToSend(message);
            Log.e("test_a","aaa");}


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

    public String byteArrayToHex(byte[] scanRecord){
        StringBuilder sb= new StringBuilder();
        //Log.e("tag2", Arrays.toString(scanRecord));
        for(final byte b: scanRecord)
            sb.append(String.format("%02x ", b));
        return sb.toString();
    }

    private static String findData(String[] hexArray,int n) {
        boolean found = false;
        StringBuilder result = new StringBuilder();

        if (n == 1) {       //TimeOTP 찾을 때
            for (int i = 0; i < hexArray.length - 2; i++) {
                if (hexArray[i].equals("f0") && hexArray[i + 1].equals("f0")) {
                    // "f0 f0" 다음의 데이터를 추출 (다음 3개의 요소)

                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 2], 16)));

                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 3], 16)));

                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 4], 16)));
                    found = true;
                    break;
                }
            }
        }
        if (n == 2) {       //센싱데이터 찾을 때
            for (int i = 0; i < hexArray.length - 2; i++) {
                if (hexArray[i].equals("99") && hexArray[i + 1].equals("99")) {
                    // "f0 f0" 다음의 데이터를 추출 (다음 3개의 요소)
                    if (Integer.valueOf(hexArray[i + 2], 16) < 9)
                        result.append("0" + String.valueOf(Integer.valueOf(hexArray[i + 2], 16)));
                    else
                        result.append(String.valueOf(Integer.valueOf(hexArray[i + 2], 16)));
                    if (Integer.valueOf(hexArray[i + 3], 16) < 9)
                        result.append("0" + String.valueOf(Integer.valueOf(hexArray[i + 3], 16)));
                    else
                        result.append(String.valueOf(Integer.valueOf(hexArray[i + 3], 16)));
                    if (Integer.valueOf(hexArray[i + 4], 16) < 9)
                        result.append("0" + String.valueOf(Integer.valueOf(hexArray[i + 4], 16)));
                    else
                        result.append(String.valueOf(Integer.valueOf(hexArray[i + 4], 16)));
                    if (Integer.valueOf(hexArray[i + 5], 16) < 9)
                        result.append("0" + String.valueOf(Integer.valueOf(hexArray[i + 5], 16)));
                    else
                        result.append(String.valueOf(Integer.valueOf(hexArray[i + 5], 16)));
                    if (Integer.valueOf(hexArray[i + 6], 16) < 9)
                        result.append("0" + String.valueOf(Integer.valueOf(hexArray[i + 6], 16)));
                    else
                        result.append(String.valueOf(Integer.valueOf(hexArray[i + 6], 16)));

                    found = true;
                    break;
                }
            }
        }
        if (n == 3) {       //센서 데이터 찾을 때
            for (int i = 0; i < hexArray.length - 2; i++) {
                if (hexArray[i].equals("fd")) {
                    // "f0 f0" 다음의 데이터를 추출 (다음 3개의 요소)

                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 1], 16)));
                    result.append("/");

                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 2], 16)));
                    result.append("/");

                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 3], 16)));
                    found = true;
                    break;
                }
            }
        }
        if(n == 4){ //air sensor
            for (int i = 0; i < hexArray.length - 2; i++) {
                if (hexArray[i].equals("fd")) {
                    // "f0 f0" 다음의 데이터를 추출 (다음 3개의 요소)

                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 1], 16)+Integer.valueOf(hexArray[i + 2], 16)));


                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            System.out.println("Target pattern not found.");
            return ""; // 원하는 패턴이 없을 경우 빈 문자열 반환
        }

        return result.toString();
    }
}

