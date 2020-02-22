package com.kushcabbage.iotserviceswitch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class HttpService extends Service {

    String ip;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Debug","Start HTTP service");
        ip = intent.getStringExtra("ip");
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        boolean value = intent.getBooleanExtra("value",true);

        if(value){
            Log.d("Debug","Turn on!");
            HttpGet http = new HttpGet(ip+"/on",getApplicationContext());
            http.execute();

        }else{
            Log.d("Debug","Turn off!");
            HttpGet http = new HttpGet(ip+"/off",getApplicationContext());
            http.execute();
        }
        //stopSelf();
        return super.onStartCommand(intent, flags, startId);


    }



}
