package com.example.dust_sensor_connection;


import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String messageToSend ="";


    public ConnectedThread(BluetoothSocket socket) {
        // 다른 기기와의 Bluetooth connection을 관리하는 클래스
        // InputStream : 데이터 수신시 사용
        // OutputStream : 데이터 전송시 사용
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;


        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e){
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024]; //buffer store for the stream
        int bytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        while(true){
            try{
                // Read from the InputStream
                Log.e("test", String.valueOf(mmInStream));
                //은성오빠코드
                if(!messageToSend.isEmpty()){
                    write(messageToSend);
                    messageToSend = ""; //Reset message after sending
                }
                bytes = mmInStream.available();

                if (bytes != 0){
                    buffer = new byte[1024];
                    SystemClock.sleep(100); // pause and wait for rest of data.
                    bytes = mmInStream.available(); // how many bytes are ready
                    bytes = mmInStream.read(buffer, 0 ,bytes); //record how many
                    String data = new String(buffer, StandardCharsets.UTF_8);
                    Log.e("data", data);
                }
            } catch (IOException e){
                e.printStackTrace();

                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String input) {
        // 데이터를 connect 된 기기로 전송하는 부분
        byte[] bytes = input.getBytes();
        try{
            mmOutStream.write(bytes);
        }catch (IOException e){
        }

        }
    //은성오빠코드
    public synchronized void setMessageToSend(String message){
        this.messageToSend = message;
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        //현재 connection을 종료하는 부분
        try{
            mmSocket.close();
        } catch (IOException e){
        }
    }
}