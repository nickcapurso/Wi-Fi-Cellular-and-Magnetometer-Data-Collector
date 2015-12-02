package capurso.io.datacollector.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import capurso.io.datacollector.R;
import capurso.io.datacollector.SimpleFileDialog;

/**
 * Created by cheng on 12/1/15.
 */
public abstract class ScanFragment extends Fragment implements View.OnClickListener, SimpleFileDialog.SimpleFileDialogListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        CardView button = (CardView)view.findViewById(R.id.btnScan);
        button.setOnClickListener(this);

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
                startScanning(null);
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
        } catch (FileNotFoundException e){
            printer = null;
        }
        startScanning(printer);
    }

    protected abstract void startScanning(PrintWriter outputFile);
    protected abstract void stopScanning();
    protected abstract boolean readyToScan();
}
