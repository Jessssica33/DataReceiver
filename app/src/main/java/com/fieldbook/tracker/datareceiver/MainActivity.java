package com.fieldbook.tracker.datareceiver;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mDrop1, mDrop2, mDrop3;
    BluetoothClient mBluetoothClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrop1 = (TextView) findViewById(R.id.drop1);
        mDrop2 = (TextView) findViewById(R.id.drop2);
        mDrop3 = (TextView) findViewById(R.id.drop3);

        mBluetoothClient = new BluetoothClient(mHandler);
        mBluetoothClient.init();
    }

    private void setText(String s) {
        if (s.startsWith("drop1")) {
            mDrop1.setText(s);
        } else if (s.startsWith("drop2")) {
            mDrop2.setText(s);
        } else if (s.startsWith("drop3")) {
            mDrop3.setText(s);
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
            }
        }
    };
}
