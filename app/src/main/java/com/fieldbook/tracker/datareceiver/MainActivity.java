package com.fieldbook.tracker.datareceiver;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView uxDrop1, uxDrop2, uxDrop3, uxPlot, uxTrait, uxMode;
    private Button uxSettingButton;
    private ImageView uxStatus;

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private String mDeviceName = "";
    private BluetoothDevice mDevice;
    private VoiceCmdReceiver mVoiceCmdReceiver;

    private final String TAG = "MAINACTIVITY";
    private int isConnected = 0;   //0 means no connection, 1 means connect success

    //TextView uxMode;
    BluetoothClient mBluetoothClient;
    int mode = 0; //0: trait 1: plot

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUIInstance();

        //deal with bluetooth adapter and paired devices
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices = mBluetoothAdapter.getBondedDevices();

        mBluetoothClient = new BluetoothClient(mHandler);

        uxSettingButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                uxSettingButton.setBackgroundColor(getResources().getColor(R.color.colorDarkGreen));
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra("devices", getPairedDeviceName());
                startActivity(intent);
            }
        });

        mVoiceCmdReceiver = new VoiceCmdReceiver(this);
        setMode();
    }

    public void setMode() {
        if (mode == 0) {
            uxMode.setText("TRAIT");
        } else if (mode == 1) {
            uxMode.setText("PLOT");
        }
    }

    private void initUIInstance() {

        uxDrop1 = (TextView) findViewById(R.id.drop1);
        uxDrop2 = (TextView) findViewById(R.id.drop2);
        uxDrop3 = (TextView) findViewById(R.id.drop3);
        uxPlot = (TextView) findViewById(R.id.plot);
        uxTrait = (TextView) findViewById(R.id.trait);
        uxStatus = (ImageView) findViewById(R.id.status);

        uxMode = (TextView) findViewById(R.id.modeInfo);
        uxSettingButton = (Button) findViewById(R.id.setting);
    }

    private boolean getDevice() {

        if (mDeviceName.equals("-1") || mDeviceName.trim().isEmpty()) {
            return false;
        }

        for (BluetoothDevice device: mPairedDevices) {
            if (device.getName().equals(mDeviceName)) {
                mDevice = device;
                return true;
            }
        }

        return false;
    }

    private String[] getPairedDeviceName(){
        ArrayList<String> names = new ArrayList<>();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice device: mPairedDevices) {
                names.add(device.getName());
            }
        }

        String[] tmp = new String[names.size()];
        for (int i = 0; i < names.size(); ++i) {
            tmp[i] = names.get(i);

        }
        return tmp;
    }

    //true means do not change device, false means change new device for connection
    private boolean getConnectDeviceFromSetting() {
        SharedPreferences shardPref = PreferenceManager.getDefaultSharedPreferences(this);
        String choose = shardPref.getString("deviceList", "-1");
        Toast.makeText(this, choose, Toast.LENGTH_SHORT).show();

        if (mDeviceName.equals(choose)) {
            Log.i(TAG, "Do not change the connect device");
            mDeviceName = choose;
            return true;
        } else {
            Log.i(TAG, "Change connect device, should re-connect it");
            mDeviceName = choose;
            return false;
        }
    }

    @Override
    protected  void onResume() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        uxSettingButton.setBackgroundColor(getResources().getColor(R.color.colorLightGreen));

        Boolean con = getConnectDeviceFromSetting();
        if (con == false  || isConnected == 0) {
            if (getDevice()) {
                mBluetoothClient.cancel();
                mBluetoothClient.init(mDevice, mBluetoothAdapter);
            }
        }
        setMode();
        super.onResume();
    }

    @Override
    protected  void onDestroy() {
        mVoiceCmdReceiver.unregister();
        super.onDestroy();
    }

    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void setText(String s) {

        String[] info = s.split("&");

        for (int i = 0; i < info.length; ++i) {
            String tmp = info[i].trim();
            if (tmp.startsWith("drop1")) {
                uxDrop1.setText(tmp.replace("drop1", "").trim());
            } else if (tmp.startsWith("drop2")) {
                uxDrop2.setText(tmp.replace("drop2", "").trim());
            } else if (tmp.startsWith("drop3")) {
                uxDrop3.setText(tmp.replace("drop3", "").trim());
            } else if (tmp.startsWith("plot")) {
                uxPlot.setText(tmp.replace("plot", "").trim());
            } else if (tmp.startsWith("trait")) {
                uxTrait.setText(tmp.trim());
            }
        }
    }

    /*private void setText(String s) {

        String[] data = s.split(" ");
        if (data.length < 3) {
            Log.i("info", s);
            return;
        }

        int index = 0;
        String tmp = "";
        while (index < data.length) {
            if (data[index].equals("drop1") && ((index + 2) < data.length)) {
                mDrop1.setText(data[index + 1]);
                tmp = "";
                index += 2;
                while (index < data.length && !data[index].contains("drop")) {
                    tmp += data[index] + " ";
                    index++;
                }
                mContent1.setText(tmp);
            } else if (data[index].equals("drop2") && ((index + 2) < data.length)) {
                mDrop2.setText(data[index + 1]);
                index += 2;
                tmp = "";
                while (index < data.length && !data[index].contains("drop")) {
                    tmp += data[index] + " ";
                    index++;
                }
                mContent2.setText(tmp);

            }else if (data[index].equals("drop3") && ((index + 2) < data.length)) {
                mDrop3.setText(data[index + 1]);
                tmp = "";
                index += 2;
                while (index < data.length && !data[index].contains("drop")) {
                    tmp += data[index] + " ";
                    index++;
                }
                mContent3.setText(tmp);
            } else {
                ++index;
            }

        }
    }*/

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch(msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    //String readMessage = new String(readBuf);
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.i(TAG, "Receive data from Field Book: " + readMessage);
                    setText(readMessage);
                    break;
                case MessageConstants.MESSAGE_CONNECT:
                    uxStatus.setImageResource(R.drawable.ic_connected);
                    isConnected = 1;
                    break;
                case MessageConstants.MESSAGE_FAILED_CONNECT:
                    uxStatus.setImageResource(R.drawable.ic_disconnected);
                    isConnected = 0;
                    break;
            }
        }
    };

    //android:icon="@mipmap/ic_launcher"
}
