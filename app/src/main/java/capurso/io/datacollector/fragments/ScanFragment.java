package capurso.io.datacollector.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import capurso.io.datacollector.R;
import capurso.io.datacollector.SimpleFileDialog;
import capurso.io.datacollector.common.Utils;

/**
 * Abstract class for scan-based fragments due to shared behavior.
 */
public abstract class ScanFragment extends Fragment implements View.OnClickListener, SimpleFileDialog.SimpleFileDialogListener {
    /**
     * LogCat tag.
     */
    private static final String TAG = ScanFragment.class.getName();

    /**
     * Reference to the preferences' file.
     */
    private SharedPreferences mPrefs;

    /**
     * Reference to the "Start/Stop Scan" button
     */
    private CardView mBtnScan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        mBtnScan = (CardView)view.findViewById(R.id.btnScan);
        mBtnScan.setOnClickListener(this);

        mPrefs = getActivity().getSharedPreferences(Utils.PREFS_NAME, 0);

        return view;
    }

    /**
     * Flip the button text and prepare to open the output file.
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnScan) {
            CardView button = (CardView) v;
            TextView buttonLabel = (TextView) v.findViewById(R.id.tvBtnLabel);

            if (buttonLabel.getText().toString().equals(getString(R.string.start_scan)) && readyToScan()) {
                //Flip button text to "Stop Scanning"
                button.setCardBackgroundColor(
                        getResources().getColor(R.color.material_red_500));
                buttonLabel.setText(getString(R.string.stop_scan));

                //Check the preferences' file to see if we should skip the save file dialog
                if(!mPrefs.getBoolean(Utils.PREFS_KEY_NEVERASK_PATH, false)){
                    //Show the dialog
                    Utils.showSaveFileDialog(getActivity(), Utils.getDefaultFilePath(),
                            Utils.getDefaultFileName(getDataType()), this);
                }else{
                    //Otherwise open the file on the default path
                    PrintWriter printer;

                    try{
                        String path = Utils.getDefaultFilePath() + "/" + Utils.getDefaultFileName(getDataType());
                        Toast.makeText(getActivity(), getString(R.string.using_default_path) + path, Toast.LENGTH_LONG).show();
                        printer = new PrintWriter(path);
                    } catch (FileNotFoundException e){
                        printer = null;
                    }

                    startScanning(printer);
                }
            }else {
                //Flip button text to "Start Scanning"
                button.setCardBackgroundColor(
                        getResources().getColor(R.color.material_green_500));
                buttonLabel.setText(getString(R.string.start_scan));

                stopScanning();
            }
        }
    }

    /**
     * Attempt to open/create the file that has been selected, then begin scanning
     * @param chosenDir
     */
    @Override
    public void onChosenDir(String chosenDir){
        PrintWriter printer;
        Log.d(TAG, "Chosen dir: " + chosenDir);

        try{
            printer = new PrintWriter(chosenDir);
            Toast.makeText(getActivity(), getString(R.string.using_path) + chosenDir, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            printer = null;
        }

        startScanning(printer);
    }

    /**
     * Flip the button back to "Start Scanning" if the user cancelled the dialog.
     */
    @Override
    public void onDialogCanceled(){
        mBtnScan.setCardBackgroundColor(
                getResources().getColor(R.color.material_green_500));

        TextView buttonLabel = (TextView) mBtnScan.findViewById(R.id.tvBtnLabel);
        buttonLabel.setText(getString(R.string.start_scan));
    }

    protected abstract void startScanning(PrintWriter outputFile);
    protected abstract void stopScanning();
    protected abstract boolean readyToScan();
    protected abstract String getDataType();
}
