package capurso.io.datacollector.fragments.wifi;

import capurso.io.datacollector.common.Utils;

/**
 * Created by cheng on 12/1/15.
 */
public class WifiInfo {
    public String ssid, mac, rss, timestamp;

    public WifiInfo(String ssid, String mac, String rss, String timestamp){
        this.ssid = ssid;
        this.mac = mac;
        this.rss = rss;
        this.timestamp = timestamp;
    }

    @Override
    public String toString(){
        return new StringBuilder(ssid)
                .append(Utils.FIELD_DELIMITER)
                .append(mac)
                .append(Utils.FIELD_DELIMITER)
                .append(rss)
                .append(Utils.FIELD_DELIMITER)
                .append(timestamp)
                .append("\n")
                .toString();
    }
}
