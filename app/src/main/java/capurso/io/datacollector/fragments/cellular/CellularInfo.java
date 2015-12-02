package capurso.io.datacollector.fragments.cellular;

import capurso.io.datacollector.common.Utils;

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

    @Override
    public boolean equals(Object o){
        return o instanceof CellularInfo && ((CellularInfo)o).towerId.equals(towerId);
    }

    @Override
    public String toString(){
        return new StringBuilder(towerId)
                .append(Utils.FIELD_DELIMITER)
                .append(type)
                .append(Utils.FIELD_DELIMITER)
                .append(rss)
                .append(Utils.FIELD_DELIMITER)
                .append(timestamp)
                .append("\n")
                .toString();
    }
}
