package com.example.dust_sensor_connection;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String messageToSend = "";
    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch(IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;
        int counter = 0;

        while(true){
            try{

                Log.e("test", String.valueOf(mmInStream));
                if (!messageToSend.isEmpty()) {
                    write(messageToSend);
                    messageToSend = ""; // Reset message after sending
                }
                bytes = mmInStream.available();

                if(bytes !=0){
                    buffer = new byte[1024];
                    SystemClock.sleep(100);
                    bytes = mmInStream.available();
                    bytes = mmInStream.read(buffer, 0, bytes);
                    String data = new String(buffer, StandardCharsets.UTF_8);
                    Log.e("data",data);



                }
            } catch(IOException e) {
                e.printStackTrace();

                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String input) {
        byte[] bytes = input.getBytes();
        try{
            mmOutStream.write(bytes);
        }catch(IOException e) {
        }
    }
    public synchronized void setMessageToSend(String message) {
        this.messageToSend = message;
    }
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try{
            mmSocket.close();
        }catch (IOException e) {

        }
    }
}