package com.kushcabbage.iotserviceswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    iNotifications notifyInterface;
    String ssid, ipaddress, lastconnectedssid;

    public WifiBroadcastReceiver(iNotifications igt){
        notifyInterface =igt;
        ssid=igt.getSsid();
        ipaddress=igt.getIp();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            //TODO check wifi name matches the saved ssid in prefs
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String currentssid  = info.getSSID();

            currentssid = currentssid.substring(1,currentssid.length()-1);
            if (currentssid.equals(lastconnectedssid)){return;}
            lastconnectedssid=currentssid;
            Log.d("Debug","wifi changed to "+currentssid);
            boolean displayNotif=false;

            if (!MainActivity.skipSSIDCheck) {
                if (ssid.contains(",")) {     //multiple ssid to match
                    String[] ssids = ssid.split(",");
                    for (String name : ssids) {
                        if (currentssid.equals(name)) {
                            displayNotif = true;
                            break;
                        }
                    }
                } else {          //single to match
                    if (currentssid.equals(ssid)) {
                        displayNotif = true;
                    }
                }

                if (displayNotif) {
                    notifyInterface.reDrawNotif(false, ssid, "", "");
                } else {
                    Log.d("Debug", "dont show big notification");
                    notifyInterface.reDrawNotif(true, ssid, "", "");
                }

            } else {
                notifyInterface.reDrawNotif(false, ssid, "", "");
            }
        }
    }
}
