package com.fieldbook.tracker.datareceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.vuzix.sdk.speechrecognitionservice.VuzixSpeechClient;
public class VoiceCmdReceiver extends BroadcastReceiver{

    final String TRAIT = "TRAIT";
    final String PLOT = "PLOT";
    final String NEXT = "next";
    final String BACK = "back";

    final String TAG = "VOICECMDRECEIVER";
    private MainActivity mMainActivity;

    public VoiceCmdReceiver(MainActivity activity) {
        mMainActivity = activity;
        mMainActivity.registerReceiver(this, new IntentFilter(VuzixSpeechClient.ACTION_VOICE_COMMAND));

        try {
            VuzixSpeechClient sc = new VuzixSpeechClient(activity);
            sc.deletePhrase("*");

            sc.insertPhrase("Trait", TRAIT);
            sc.insertPhrase("Plot", PLOT);
            sc.insertPhrase("Next", NEXT);
            sc.insertPhrase("Back", BACK);
            VuzixSpeechClient.EnableRecognizer(mMainActivity, true);
        } catch(NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(VuzixSpeechClient.ACTION_VOICE_COMMAND)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {

                if (extras.containsKey(VuzixSpeechClient.PHRASE_STRING_EXTRA)) {
                    String phrase = intent.getStringExtra(VuzixSpeechClient.PHRASE_STRING_EXTRA);
                    Log.i(TAG,  "Detecte voice command: " + phrase);
                    if (phrase.equals(TRAIT)) {
                        mMainActivity.mode = 0;
                    } else if (phrase.equals(PLOT)) {
                        mMainActivity.mode = 1;
                    } else {
                        String action = mMainActivity.mode + " " + phrase;
                        mMainActivity.mBluetoothClient.write(action.getBytes());
                    }

                    mMainActivity.setMode();
                    mMainActivity.showToast(phrase);
                }
            }
        }
    }

    public void unregister() {
        try{
            mMainActivity.unregisterReceiver(this);
            mMainActivity = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
