package com.kushcabbage.iotserviceswitch;

public interface iNotifications {

    void reDrawNotif(boolean smallmode,String contentText, String title, String ticker);
   String getSsid();
   String getIp();
}
