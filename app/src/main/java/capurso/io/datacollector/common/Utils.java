package capurso.io.datacollector.common;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Environment;

import java.io.IOException;
import java.util.Calendar;

import capurso.io.datacollector.SimpleFileDialog;

/**
 * Created by cheng on 12/1/15.
 */
public class Utils {
    public static final long ONE_SECOND = 1000;
    public static final long DEFAULT_SCAN_INTERVAL = 3 * ONE_SECOND;
    public static int DEFAULT_SAMPLING_RATE = SensorManager.SENSOR_DELAY_UI;

    public static int AP_LIST_CLEAR_INTERVAL = 4;
    public static int CELL_LIST_CLEAR_INTERVAL = 8;

    public static final String DEFAULT_FILENAME_DELIMITER = "_";
    public static final String DEFAULT_FILE_EXTENSION = ".txt";

    public static final String FIELD_DELIMITER = ",";

    public static final int MAGNETIC_GRAPH_WINDOW_SIZE = 50;

    public static final String DATATYPE_WIFI = "WiFi";
    public static final String DATATYPE_CELLULAR = "Cellular";
    public static final String DATATYPE_MAGNETIC = "Magnetic";

    public static final String PREFS_NAME = "DataCollectorPrefs";
    public static final String PREFS_KEY_NEVERASK_PATH = "never_ask_filepath";

    public static String getDefaultFilePath(){
        String documentsPath = "";
        try {
            documentsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getCanonicalPath();
        }catch(IOException error){
        }
        return documentsPath;
    }

    public static String getDefaultFileName(String dataType){
        Calendar now = Calendar.getInstance();
        int month = Calendar.MONTH + 1;
        int day = Calendar.DAY_OF_MONTH;

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        String strMonth = (month < 10)? new StringBuilder("0").append(Integer.toString(month)).toString()
                : Integer.toString(month);
        String strDay = (day < 10)? new StringBuilder("0").append(Integer.toString(day)).toString()
                : Integer.toString(day);
        String strHour = (hour < 10)? new StringBuilder("0").append(Integer.toString(hour)).toString()
                : Integer.toString(hour);
        String strMinute = (minute < 10)? new StringBuilder("0").append(Integer.toString(minute)).toString()
                : Integer.toString(minute);
        String strSecond = (second < 10)? new StringBuilder("0").append(Integer.toString(second)).toString()
                : Integer.toString(second);

        return new StringBuilder(strMonth)
                .append(DEFAULT_FILENAME_DELIMITER)
                .append(strDay)
                .append(DEFAULT_FILENAME_DELIMITER)
                .append(dataType)
                .append(DEFAULT_FILENAME_DELIMITER)
                .append(strHour)
                .append(DEFAULT_FILENAME_DELIMITER)
                .append(strMinute)
                .append(DEFAULT_FILENAME_DELIMITER)
                .append(strSecond)
                .append(DEFAULT_FILE_EXTENSION)
                .toString();
    }

    public static String getTimestamp(){
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        String strHour = (hour < 10)? new StringBuilder("0").append(Integer.toString(hour)).toString()
                : Integer.toString(hour);
        String strMinute = (minute < 10)? new StringBuilder("0").append(Integer.toString(minute)).toString()
                : Integer.toString(minute);
        String strSecond = (second < 10)? new StringBuilder("0").append(Integer.toString(second)).toString()
                : Integer.toString(second);

        return new StringBuilder(strHour).append(":").append(strMinute).append(":")
            .append(strSecond).toString();
    }

    private void showSaveFileDialog(Context context, SimpleFileDialog.SimpleFileDialogListener listener){
        SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(context, "FileOpen", listener);

        //You can change the default filename using the public variable "Default_File_Name"
        FileOpenDialog.Default_File_Name = Utils.getDefaultFileName(Utils.DATATYPE_WIFI);

        try {
            FileOpenDialog.chooseFile_or_Dir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getCanonicalPath());
        }catch(IOException e){

        }
    }
}
