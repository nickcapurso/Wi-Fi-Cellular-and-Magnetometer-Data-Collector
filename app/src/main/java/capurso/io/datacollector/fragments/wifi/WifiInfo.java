package capurso.io.datacollector.fragments.wifi;

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
}
