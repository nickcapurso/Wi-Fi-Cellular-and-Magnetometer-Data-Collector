package capurso.io.datacollector.fragments.cellular;

/**
 * Created by cheng on 12/1/15.
 */
public class CellularInfo {
    public static final String TYPE_CDMA = "CDMA";
    public static final String TYPE_LTE = "LTE";
    public static final String TYPE_GSM = "GSM";
    public static final String TYPE_WCDMA = "WCDMA";
    public String towerId, type, rss, timestamp;

    public CellularInfo(String towerId, String type, String rss, String timestamp){
        this.towerId = towerId;
        this.type = type;
        this.rss = rss;
        this.timestamp = timestamp;
    }
}
