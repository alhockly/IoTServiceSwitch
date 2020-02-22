package com.kushcabbage.iotserviceswitch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


public class MonitorNetworkService extends Service implements iNotifications {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public int notificationID=1;
    WifiBroadcastReceiver wifibroadcast;
    NotificationManager notifManager;
    public String ipaddress;
    public String ssid;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ipaddress = intent.getStringExtra("ip");
        ssid = intent.getStringExtra("ssid");
        wifibroadcast  = new WifiBroadcastReceiver(this);
        notifManager = getSystemService(NotificationManager.class);
        Log.d("Debug","Service Started");
        createNotificationChannel();


        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        //.setContentTitle("Wifi Service")
                        //.setContentText("Service is running")
                        .setSmallIcon(R.drawable.ic_action_signal)
                        //.setTicker("Ticker text")
                        .build();

        startForeground(notificationID, notification);

        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(wifibroadcast, filter);

        return START_STICKY;

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifibroadcast);
        Log.d("Debug","onDestroy @ Monitor Network");

    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );


            notifManager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void reDrawNotif(boolean smallmode, String contentText, String title, String ticker) {

        Icon icon = Icon.createWithResource("package",R.drawable.ic_launcher_foreground);
        Notification notification;
        if(smallmode){

            notification = new Notification.Builder(this,CHANNEL_ID)
                    .setSmallIcon(icon)
                    .build();

        } else {

            Intent turnOnIntent = new Intent(this, HttpService.class);
            turnOnIntent.setAction("turnOn");
            turnOnIntent.putExtra("ip", ipaddress);
            turnOnIntent.putExtra("value", true);
            PendingIntent onPendingIntent = PendingIntent.getService(this, (int) System.currentTimeMillis(), turnOnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Notification.Action onAction = new Notification.Action.Builder(icon, "ON", onPendingIntent).build();

            Intent turnOffIntent = new Intent(this, HttpService.class);
            turnOffIntent.setAction("turnOff");
            turnOffIntent.putExtra("ip", ipaddress);
            turnOffIntent.putExtra("value", false);
            PendingIntent offPendingIntent = PendingIntent.getService(this, (int) System.currentTimeMillis(), turnOffIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Notification.Action offAction = new Notification.Action.Builder(icon, "OFF", offPendingIntent).build();

            notification =  new Notification.Builder(this, CHANNEL_ID)
                            .setContentTitle("Wifi Service")
                            .setContentText(ipaddress)
                            .addAction(onAction)
                            .addAction(offAction)
                            .setSmallIcon(R.drawable.ic_action_signal)
                            .setTicker("Ticker text")
                            .setAutoCancel(true)
                            .build();
        }
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(notificationID, notification);

    }

    @Override
    public String getSsid() {
        return ssid;
    }

    @Override
    public String getIp() {
        return ipaddress;
    }


}
