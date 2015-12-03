package capurso.io.datacollector.common;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.util.Calendar;

import capurso.io.datacollector.SimpleFileDialog;
import capurso.io.datacollector.EditTextDialog;

/**
 * Constants and utility functions.
 */
public class Utils {
    /**
     * One second in milliseconds.
     */
    public static final long ONE_SECOND = 1000;

    /**
     * For Wi-Fi and cellular, this adjusts how often a scan occurs.
     * By default, it is 3 seconds. There is some overhead involved in processing a scan
     * so an interval that is too short may cause inconsistent results.
     */
    public static final long DEFAULT_SCAN_INTERVAL = 3 * ONE_SECOND;

    /**
     * Default sampling rate for the magnetometer. Uses a rate suitable for
     * graphing, but it still quite fast for logging purposes.
     */
    public static int DEFAULT_SAMPLING_RATE = SensorManager.SENSOR_DELAY_UI;

    /**
     * Controls how often to clear the visual list of AP scan results. Does
     * not affect the data which is written to file. Default value is
     * every 4 intervals (4 * DEFAULT_SCAN_INTERVAL)
     */
    public static int AP_LIST_CLEAR_INTERVAL = 4;

    /**
     * Controls how often to clear the visual list of cellular scan results. Does
     * not affect the data which is written to file. Default value is
     * every 8 intervals (8 * DEFAULT_SCAN_INTERVAL)
     */
    public static int CELL_LIST_CLEAR_INTERVAL = 8;

    /**
     * Default delimiter for filename elements (ex. 12_02_WiFi_ ... )
     */
    public static final String DEFAULT_FILENAME_DELIMITER = "_";

    /**
     * Default file extension. Can be changed by the user
     */
    public static String DEFAULT_FILE_EXTENSION = ".csv";

    /**
     * Delimiter for fields on one line of the output file.
     * For example if comma: SSID,MAC,RSS,Time
     */
    public static final String FIELD_DELIMITER = ",";

    /**
     * Max number of points to show on the magnetometer graph at one time.
     */
    public static final int MAGNETIC_GRAPH_WINDOW_SIZE = 50;

    //Datatype strings for default filenames.
    public static final String DATATYPE_WIFI = "WiFi";
    public static final String DATATYPE_CELLULAR = "Cellular";
    public static final String DATATYPE_MAGNETIC = "Magnetic";

    /**
     * Preferences filename.
     */
    public static final String PREFS_NAME = "DataCollectorPrefs";

    /**
     * Key for the "Always Use Default Filename" option.
     */
    public static final String PREFS_KEY_NEVERASK_PATH = "never_ask_filepath";

    /**
     * Key for the default file extension
     */
    public static final String PREFS_KEY_FILE_EXTENSION = "file_extension";

    /**
     * Returns the default file path - the Documents folder.
     * @return String path to the phone's Documents folder.
     */
    public static String getDefaultFilePath(){
        String documentsPath = "";
        try {
            documentsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getCanonicalPath();
        }catch(IOException error){
        }
        return documentsPath;
    }

    /**
     * Returns the default file name = MONTH_DAY_DATATYPE_HOUR_MINUTE_SECOND.extension
     * @param dataType WiFi, Cellular, or Magnetic
     * @return Default file name
     */
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

    /**
     * Returns a formatted timestamp of the form HOUR:MINUTE:SECOND
     * @return timestamp
     */
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

    /**
     * Opens a save file dialog at the specified path and with a default filename.
     * @param context
     * @param filePath
     * @param fileName
     * @param listener
     */
    public static void showSaveFileDialog(Context context, String filePath, String fileName,
                                    SimpleFileDialog.SimpleFileDialogListener listener){
        SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(context, "FileOpen", listener);

        //You can change the default filename using the public variable "Default_File_Name"
        FileOpenDialog.Default_File_Name = fileName;
        FileOpenDialog.chooseFile_or_Dir(filePath);
    }

    /**
     * Starts the dialog for chaning the default file extension.
     * @param activity To attach to
     */
    public static void showExtensionDialog(AppCompatActivity activity){
        Bundle args = new Bundle();
        args.putString(EditTextDialog.ARG_CURR_EXT, DEFAULT_FILE_EXTENSION);

        EditTextDialog dialog = new EditTextDialog();
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), "Change Extension Dialog");
    }
}
