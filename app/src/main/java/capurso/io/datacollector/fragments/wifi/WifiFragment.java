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
 * Created by cheng on 12/1/15.
 */
public class WifiFragment extends ScanFragment{
    private static final String TAG = WifiFragment.class.getName();

    private static final IntentFilter mIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    private RecyclerView mRvScanResults;
    private WifiInfoAdapter mAdapter;
    private List<WifiInfo> mWifiInfos;

    private WifiManager mWifiManager;
    private BroadcastReceiver mReceiver;

    private Timer mScanTimer = null;

    private PrintWriter mPrinter;

    private int mIntervalCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = super.onCreateView(inflater, container, savedInstanceState);

        //Set up RecyclerView and data model list
        mRvScanResults = (RecyclerView)view.findViewById(R.id.rvScanResults);
        mRvScanResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        mWifiInfos = new ArrayList<>();
        mAdapter = new WifiInfoAdapter(mWifiInfos, getActivity());
        mRvScanResults.setAdapter(mAdapter);

        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        mReceiver = new WifiBroadcastReceiver();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }



    @Override
    protected void startScanning(PrintWriter outputFile) {
        if(outputFile == null){
            Toast.makeText(getActivity(), getString(R.string.fileerror), Toast.LENGTH_LONG).show();
            return;
        }

        mPrinter = outputFile;

        Log.d(TAG, "Starting continuous wifi scan");
        mScanTimer = new Timer();
        mScanTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mWifiManager.startScan();

                if(mIntervalCount == Utils.AP_LIST_CLEAR_INTERVAL){
                    mIntervalCount = 0;
                    mWifiInfos.clear();
                }

                mIntervalCount++;
            }
        }, 0, Utils.DEFAULT_SCAN_INTERVAL);
    }

    @Override
    protected void stopScanning() {
        Log.d(TAG, "Stopping continuous wifi scan");
        if(mScanTimer != null) {
            mScanTimer.cancel();
            mScanTimer = null;

            if(mPrinter != null){
                mPrinter.close();
                mPrinter = null;
            }
        }
    }

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

    private void processScanResults(List<ScanResult> results) {
        if(results == null)
            return;

        for (ScanResult result : results){
            if(result.SSID == null || result.SSID.equals(""))
                continue;

            WifiInfo info = new WifiInfo(result.SSID, result.BSSID, "" + result.level, Utils.getTimestamp());
            mWifiInfos.add(info);

            if(mPrinter !=  null) {
                String s = info.toString();
                mPrinter.print(s);
                Log.d(TAG, s);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

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
