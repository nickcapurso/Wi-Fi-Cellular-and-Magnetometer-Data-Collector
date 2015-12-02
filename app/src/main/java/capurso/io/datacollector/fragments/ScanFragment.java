package capurso.io.datacollector.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
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
 * Created by cheng on 12/1/15.
 */
public abstract class ScanFragment extends Fragment implements View.OnClickListener, SimpleFileDialog.SimpleFileDialogListener {
    private static final String TAG = ScanFragment.class.getName();
    private SharedPreferences mPrefs;
    private String mDataType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        CardView button = (CardView)view.findViewById(R.id.btnScan);
        button.setOnClickListener(this);

        mPrefs = getActivity().getSharedPreferences(Utils.PREFS_NAME, 0);

        return view;
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnScan) {
            CardView button = (CardView) v;
            TextView buttonLabel = (TextView) v.findViewById(R.id.tvBtnLabel);

            if (buttonLabel.getText().toString().equals(getString(R.string.start_scan)) && readyToScan()) {
                button.setCardBackgroundColor(
                        getResources().getColor(R.color.material_red_500));
                buttonLabel.setText(getString(R.string.stop_scan));

                if(!mPrefs.getBoolean(Utils.PREFS_KEY_NEVERASK_PATH, false)){
                    SimpleFileDialog FileSaveDialog =  new SimpleFileDialog(getActivity(), "FileSave", this);

                    FileSaveDialog.Default_File_Name = Utils.getDefaultFileName(getDataType());
                    FileSaveDialog.chooseFile_or_Dir(Utils.getDefaultFilePath());
                }else{
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
                button.setCardBackgroundColor(
                        getResources().getColor(R.color.material_green_500));
                buttonLabel.setText(getString(R.string.start_scan));
                stopScanning();
            }
        }
    }

    @Override
    public void onChosenDir(String chosenDir){
        PrintWriter printer;

        try{
            printer = new PrintWriter(chosenDir);
            Toast.makeText(getActivity(), getString(R.string.using_path) + chosenDir, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            printer = null;
        }
        startScanning(printer);
    }

    protected abstract void startScanning(PrintWriter outputFile);
    protected abstract void stopScanning();
    protected abstract boolean readyToScan();
    protected abstract String getDataType();
}
