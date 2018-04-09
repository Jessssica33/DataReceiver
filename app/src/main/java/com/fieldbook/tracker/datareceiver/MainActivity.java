package com.fieldbook.tracker.datareceiver;

import android.app.ActionBar;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mDrop1, mDrop2, mDrop3, mContent1, mContent2, mContent3, mConnectionInfo;
    private Button mOpenedButton;
    private BluetoothClient mBluetoothClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrop1 = (TextView) findViewById(R.id.drop1);
        mDrop2 = (TextView) findViewById(R.id.drop2);
        mDrop3 = (TextView) findViewById(R.id.drop3);
        mContent1 = (TextView) findViewById(R.id.content1);
        mContent2 = (TextView) findViewById(R.id.content2);
        mContent3 = (TextView) findViewById(R.id.content3);
        mConnectionInfo = (TextView) findViewById(R.id.connectionInfo);
        mOpenedButton = (Button) findViewById(R.id.opened);

        mBluetoothClient = new BluetoothClient(mHandler);
        mBluetoothClient.init();

        mOpenedButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                mBluetoothClient.init();
            }
        });
    }


    @Override
    protected  void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    private void setText(String s) {

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
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch(msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    setText(readMessage);
                    break;
                case MessageConstants.MESSAGE_CONNECT:
                    mConnectionInfo.setText("Bluetooth connect success");
                    mOpenedButton.setClickable(false);
                    break;
                case MessageConstants.MESSAGE_FAILED_CONNECT:
                    mConnectionInfo.setText("Connection failed, please open FieldBook.");
                    mOpenedButton.setClickable(true);
                    break;
            }
        }
    };
}
