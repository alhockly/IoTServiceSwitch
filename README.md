# IoTServiceSwitch

ip adress with is the address to send http requests to

The SSID is the SSID your phone has to be connected to for the notification to appear as an expandable notification (where you can get the buttons)
 
 The requests sent based on what button is pressed are defined in `HTTPService.java:onStartCommand` and partially in `MonitorNetworkService.java:reDrawNotif`where the notifcation is defined
