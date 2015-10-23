package com.domotrix.domotrixdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TestReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "DOMOTRIX CONNECTED", Toast.LENGTH_LONG).show();
    }
}
