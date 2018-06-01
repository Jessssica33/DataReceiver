package com.fieldbook.tracker.datareceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jessica on 3/9/18.
 */

public class BluetoothClient {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final String TAG = "BluetoothClient";
    ConnectThread mConnectThread;
    ConnectedThread mConnectedThread;
    //private BluetoothSocket mSocket;

    private Handler mHandler;

    public BluetoothClient(Handler handler){
        mHandler = handler;
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void init(BluetoothDevice device, BluetoothAdapter adapter) {

        mDevice = device;
        mBluetoothAdapter = adapter;
        //This part is done in MainActivity
        /*Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device: pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.i(TAG, deviceName + ": " + deviceHardwareAddress);
                mDevice = device;
                break;
            }
        }*/

        mConnectThread = new ConnectThread(mDevice);
        mConnectThread.start();

    }

    public void write(byte[] bytes) {

        if (mConnectedThread == null) {
            Log.d("CONNECTED THREAD", "IS NULL");
            return;
        }
        mConnectedThread.write(bytes);
    }

    public void cancel() {

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }


    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {

            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmSocket = tmp;
        }

        public void run() {
            //mBluetoothAdapter.cancelDiscovery();
            int loop = 1;
            Message msg;


            try {
                mmSocket.connect();
                loop = 0;
            } catch (IOException connectEx) {
                loop = 1;
                msg = mHandler.obtainMessage(MessageConstants.MESSAGE_FAILED_CONNECT, 0, -1, null);
                msg.sendToTarget();
                connectEx.printStackTrace();
                try {
                    mmSocket.close();
                } catch (IOException closeEx) {
                    Log.e(TAG, "Could not close the client socket", closeEx);
                }

                return;
            }

            if (loop == 0) {
                msg = mHandler.obtainMessage(MessageConstants.MESSAGE_CONNECT, 0, -1, null);
                msg.sendToTarget();
            }

            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();

            if (mConnectedThread != null && (mConnectedThread.isAlive()
                    || mConnectedThread.isDaemon() || mConnectedThread.isInterrupted())) {
                //mConnectedThread.cancel();
                Log.i("info", "=======connected thread=======");
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }



    private class ConnectedThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);

                    Message msg = mHandler.obtainMessage(MessageConstants.MESSAGE_FAILED_CONNECT, 0, -1, null);
                    msg.sendToTarget();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {

            try {
                mmOutStream.write(bytes);

                //Message writeMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                //writeMsg.sendToTarget();
            }catch (IOException e) {

                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                //Message writeErrorMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                //Bundle bundle = new Bundle();
                //bundle.putString("toast", "Couldn't send data to the other device");
                //writeErrorMsg.setData(bundle);
                //mHandler.sendMessage(writeErrorMsg);
            }
        }

        public void cancel() {

            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}
