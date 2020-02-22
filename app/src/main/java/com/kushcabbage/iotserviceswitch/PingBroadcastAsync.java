package com.kushcabbage.iotserviceswitch;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PingBroadcastAsync extends AsyncTask<Void,Void,Void> {
    String broadcastAddr;
    OkHttpClient client = new OkHttpClient();
    int responsecode;
    public PingBroadcastAsync(String addr){
        broadcastAddr=addr;
    }

    @Override
    protected Void doInBackground(Void... voids) {
//        try {
//            run(broadcastAddr);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        ping(broadcastAddr);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("Debug","Ping broadcast code:"+responsecode);
        Log.d("Debug","Reading arp table");
        getMacAddressForIp("192.168.1.155");
    }

    void run(String url) throws IOException,RuntimeException {
        url="http://"+url;
        Request request = new Request.Builder()
                .url(url)
                .method("GET",null)
                .build();

        try (Response response = client.newCall(request).execute()) {
            responsecode = response.code();
        }
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

    private boolean ping(String ip){
        System.out.println("executeCommand");
        ip=ip.substring(1);
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 "+ip);
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue "+mExitValue);
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }
}
