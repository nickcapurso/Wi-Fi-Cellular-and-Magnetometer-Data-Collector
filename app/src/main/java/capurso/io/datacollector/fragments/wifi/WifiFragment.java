package capurso.io.datacollector.fragments.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import capurso.io.datacollector.R;
import capurso.io.datacollector.common.Utils;
import capurso.io.datacollector.fragments.ScanFragment;

/**
 * Periodically retrieves Wi-FI AP information, places the information into a RecyclerView, and
 * writes the information out to file.
 */
public class WifiFragment extends ScanFragment{
    /**
     * LogCat tag.
     */
    private static final String TAG = WifiFragment.class.getName();

    /**
     * IntentFilter to listen for completed Wi-Fi scans.
     */
    private static final IntentFilter mIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    /**
     * Displays Wi-Fi AP information in a table-like format.
     */
    private RecyclerView mRvScanResults;

    /**
     * Adapter for the RecyclerView.
     */
    private WifiInfoAdapter mAdapter;

    /**
     * Contains all current information being displayed in the RecyclerView (and was just written
     * out to file).
     */
    private List<WifiInfo> mWifiInfos;

    /**
     * Reference to the Wi-Fi service for conducting scans.
     */
    private WifiManager mWifiManager;

    /**
     * Receives broadcasts when Wi-Fi scans are completed.
     */
    private BroadcastReceiver mReceiver;

    /**
     * Used to periodically start Wi-Fi scans.
     */
    private Timer mScanTimer = null;

    /**
     * Writes Wi-Fi scan information to file.
     */
    private PrintWriter mPrinter;

    /**
     * Keeps track of the current interval - the RecyclerView is cleared on every 4th interval.
     */
    private int mIntervalCount = 0;

    /**
     * General object used for synchronizing access to mWifiInfos
     */
    private Object mLock;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = super.onCreateView(inflater, container, savedInstanceState);

        //Set up RecyclerView and data model list
        mRvScanResults = (RecyclerView)view.findViewById(R.id.rvScanResults);
        mRvScanResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        mWifiInfos = new ArrayList<>();
        mAdapter = new WifiInfoAdapter(mWifiInfos, getActivity());
        mRvScanResults.setAdapter(mAdapter);

        //Get reference to Wi-Fi service
        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        mReceiver = new WifiBroadcastReceiver();

        mLock = new Object();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        //Listen for Wi-Fi broadcasts
        getActivity().registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop listening for Wi-Fi broadcasts
        getActivity().unregisterReceiver(mReceiver);
    }


    /**
     * Check for valid output file, clear the data collected in the last run, and start the
     * periodic scanning process.
     * @param outputFile
     */
    @Override
    protected void startScanning(PrintWriter outputFile) {
        //If the PrintWriter is null, the super class failed to open the file for writing.
        if(outputFile == null){
            Toast.makeText(getActivity(), getString(R.string.fileerror), Toast.LENGTH_LONG).show();
            return;
        }

        mWifiInfos.clear();
        mAdapter.notifyDataSetChanged();

        mPrinter = outputFile;

        //Schedule Wi-Fi scans at periodic intervals
        Log.d(TAG, "Starting continuous wifi scan");
        mScanTimer = new Timer();
        mScanTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mWifiManager.startScan();

                //Reset the RecyclerView periodically.
                if(mIntervalCount == Utils.AP_LIST_CLEAR_INTERVAL){
                    mIntervalCount = 0;
                    synchronized (mLock) {
                        mWifiInfos.clear();
                    }
                }

                mIntervalCount++;
            }
        }, 0, Utils.DEFAULT_SCAN_INTERVAL);
    }

    /**
     * Cancel periodic scanning and close the opened file.
     */
    @Override
    protected void stopScanning() {
        Log.d(TAG, "Stopping continuous wifi scan");
        if(mScanTimer != null) {
            //Stop periodic Wi-Fi scans
            mScanTimer.cancel();
            mScanTimer = null;

            //Close the file after flushing
            if(mPrinter != null){
                mPrinter.flush();
                mPrinter.close();
                mPrinter = null;
            }

            mIntervalCount = 0;
        }
    }

    /**
     * Only begin scanning if the user has Wi-Fi enabled.
     * @return
     */
    @Override
    protected boolean readyToScan() {
        if(mWifiManager != null && mWifiManager.isWifiEnabled())
            return true;
        Toast.makeText(getActivity(), getString(R.string.wifi_disabled), Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    protected String getDataType() {
        return Utils.DATATYPE_WIFI;
    }

    /**
     * Add each scan result to the RecyclerView and write it out to file
     * @param results
     */
    private void processScanResults(List<ScanResult> results) {
        if(results == null)
            return;

        for (ScanResult result : results){
            if(result.SSID == null || result.SSID.equals(""))
                continue;

            //Create a WifiInfo object to hold the relevant pieces of the scan result
            WifiInfo info = new WifiInfo(result.SSID, result.BSSID, "" + result.level, Utils.getTimestamp());

            //Add the new data to the list if it is not being modified
            synchronized (mLock) {
                mWifiInfos.add(info);
            }

            //Write the data out to file
            if(mPrinter !=  null) {
                String s = info.toString();
                mPrinter.print(s);
                Log.d(TAG, s);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Receives Wi-Fi related broadcasts from the OS.
     */
    private class WifiBroadcastReceiver extends BroadcastReceiver {

        /**
         * If we receive a scan completed broadcast, call the method to process the results.
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && mScanTimer != null){
                Log.d(TAG, "Wifi scan finished");
                processScanResults(mWifiManager.getScanResults());
            }
        }
    }
}
