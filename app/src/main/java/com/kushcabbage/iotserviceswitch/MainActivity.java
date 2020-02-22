package com.kushcabbage.iotserviceswitch;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends Activity {

    Intent wifiService;
    Intent serviceIntent;
    String ipaddress;
    String PrefsKey="IoTPrefs";
    String ipaddressPrefKey="ipaddress";
    String ssidPrefKey="ssid";
    String ssid;
    String broadcastAddr;

    public static boolean skipSSIDCheck=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String currentssid  = info.getSSID();
        int currentIp = info.getIpAddress();
        try {
            broadcastAddr = getBroadcastAddress().toString();
            Log.d("Debug","BROAD>>>"+broadcastAddr);
            PingBroadcastAsync ping = new PingBroadcastAsync(broadcastAddr);
            ping.execute();


        } catch (IOException e) {
            e.printStackTrace();
        }


        final EditText ipEditText = findViewById(R.id.ipEditText);
        final EditText ssidEditText = findViewById(R.id.ssid);

        SharedPreferences prefs = getSharedPreferences(PrefsKey,MODE_PRIVATE);
        ipaddress = prefs.getString(ipaddressPrefKey,"127.0.0.1");
        ipEditText.setText(ipaddress);
        ssid = prefs.getString(ssidPrefKey,"");
        ssidEditText.setText(ssid);


        //TODO  to find ip of lights Node.... ping broadcast address then look at arp table

        serviceIntent = new Intent(this, MonitorNetworkService.class);
        serviceIntent.putExtra("ip",ipaddress);
        serviceIntent.putExtra("ssid",ssid);

        requestLocationPermission();

        final Button stopserviceButton = findViewById(R.id.stopServiceButton);
        stopserviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(serviceIntent);
                finishAndRemoveTask();
            }
        });


        Button restartButton = findViewById(R.id.restart);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(serviceIntent);
                ipaddress = ipEditText.getText().toString();
                serviceIntent.putExtra("ip",ipaddress);
                startForegroundService(serviceIntent);
                SharedPreferences.Editor editor = getSharedPreferences(PrefsKey,MODE_PRIVATE).edit();
                editor.putString(ipaddressPrefKey,ipaddress);
                editor.putString(ssidPrefKey,ssidEditText.getText().toString());
                editor.commit();
                editor.apply();
            }
        });

        Switch ssidCheckSwitch = findViewById(R.id.SSIDCheckSwitch);
        ssidCheckSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ssidEditText.setEnabled(false);
                    skipSSIDCheck = true;
                } else {
                    ssidEditText.setEnabled(true);
                    skipSSIDCheck = false;

                }
            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 1){
            //User allowed the location and you can read it now
            requestLocationPermission();
            startForegroundService(serviceIntent);
        }
    }

    private void requestLocationPermission() {
        //If requested permission isn't Granted yet
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request permission from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startForegroundService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        //stopService(wifiService);
        super.onDestroy();
    }

    public String getMacAddressForIp(final String ipAddress) {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"))) {
            String line;
            while ((line = br.readLine()) != null) {
                Log.d("Debug",line);
                if (line.contains(ipAddress)) {
                    final int macStartIndex = line.indexOf(":") - 2;
                    final int macEndPos = macStartIndex + 17;
                    if (macStartIndex >= 0 && macEndPos < line.length()) {
                        return line.substring(macStartIndex, macEndPos);
                    } else {
                        Log.w("MyClass", "Found ip address line, but mac address was invalid.");
                    }
                }
            }
        } catch(Exception e){
            Log.e("MyClass", "Exception reading the arp table.", e);
        }
        return null;
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));
        return InetAddress.getByAddress(quads);
    }
}
