package capurso.io.datacollector.fragments.cellular;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
public class CellularFragment extends ScanFragment{
    private static final String TAG = CellularFragment.class.getName();

    private RecyclerView mRvScanResults;
    private CellularInfoAdapter mAdapter;
    private List<CellularInfo> mCellularInfos;
    private List<CellularInfo> mUpdatedRssList;

    private TelephonyManager mTelephonyManager;
    private CellTowerListener mTowerListener;

    private PrintWriter mPrinter;

    private Timer mScanTimer = null;
    private int mIntervalCount = 0;

    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = super.onCreateView(inflater, container, savedInstanceState);

        //Set up RecyclerView and data model list
        mRvScanResults = (RecyclerView)view.findViewById(R.id.rvScanResults);
        mRvScanResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCellularInfos = new ArrayList<>();
        mUpdatedRssList = new ArrayList<>();
        mAdapter = new CellularInfoAdapter(mCellularInfos, getActivity());
        mRvScanResults.setAdapter(mAdapter);

        mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        mTowerListener = new CellTowerListener();

        mHandler = new Handler();
        return view;
    }

    @Override
    protected void startScanning(PrintWriter outputFile) {
        if(outputFile == null){
            Toast.makeText(getActivity(), getString(R.string.fileerror), Toast.LENGTH_LONG).show();
            return;
        }

        mPrinter = outputFile;

        mScanTimer = new Timer();
        mScanTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //mWifiManager.startScan();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        processCellInfoList(mTelephonyManager.getAllCellInfo(), false);
                    }
                });

                if(mIntervalCount == Utils.CELL_LIST_CLEAR_INTERVAL){
                    mIntervalCount = 0;
                    mCellularInfos.clear();
                }

                mIntervalCount++;
            }
        }, 0, Utils.DEFAULT_SCAN_INTERVAL);

        Log.d(TAG, "Starting to listen for cell info");
        mTelephonyManager.listen(
                mTowerListener,
                PhoneStateListener.LISTEN_CELL_INFO);

    }

    @Override
    protected void stopScanning() {
        Log.d(TAG, "Stopping listening for cell info");

        mTelephonyManager.listen(
                mTowerListener,
                PhoneStateListener.LISTEN_NONE);

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
        return true;
    }

    @Override
    protected String getDataType() {
        return Utils.DATATYPE_CELLULAR;
    }

    private CellularInfo processCdmaInfo(CellInfoCdma info){
        int id = info.getCellIdentity().getBasestationId();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_CDMA;
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    private CellularInfo processGsmInfo(CellInfoGsm info){
        int id = info.getCellIdentity().getCid();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_GSM;
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    private CellularInfo processLteInfo(CellInfoLte info){
        int id = info.getCellIdentity().getCi();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_LTE;
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    private CellularInfo processWcdmaInfo(CellInfoWcdma info){
        int id = info.getCellIdentity().getCid();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_WCDMA;
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    private void processCellInfoList(List<CellInfo> infoList, boolean updateExisting){
        int existingIndex = 0;
        if(infoList == null || infoList.size() == 0)
            return;

        for (CellInfo i : infoList) {
            CellularInfo newInfo = null;

            if (i instanceof CellInfoCdma) {
                newInfo = processCdmaInfo((CellInfoCdma) i);
            } else if (i instanceof CellInfoGsm) {
                newInfo = processGsmInfo((CellInfoGsm) i);
            } else if (i instanceof CellInfoLte) {
                newInfo = processLteInfo((CellInfoLte) i);
            } else if (i instanceof CellInfoWcdma) {
                newInfo = processWcdmaInfo((CellInfoWcdma) i);
            }

            if(newInfo == null)
                continue;

            if(updateExisting){
                //Log.d(TAG, "Updating existing info for tower: " + newInfo.towerId + ", rss: " + newInfo.rss);
                existingIndex = mUpdatedRssList.indexOf(newInfo);

                if(existingIndex != -1)
                    mUpdatedRssList.set(existingIndex, newInfo);
                else
                    mUpdatedRssList.add(newInfo);
            }else{
                existingIndex = mUpdatedRssList.indexOf(newInfo);
                if(existingIndex != -1) {
                    CellularInfo existingInfo = mUpdatedRssList.get(existingIndex);
                    mCellularInfos.add(existingInfo);

                    if(mPrinter !=  null) {
                        String s = existingInfo.toString();
                        mPrinter.print(s);
                        Log.d(TAG, s);
                    }
                }else {
                    mCellularInfos.add(newInfo);

                    if(mPrinter !=  null) {
                        String s = newInfo.toString();
                        mPrinter.print(s);
                        Log.d(TAG, s);
                    }
                }
            }
        }

        if(!updateExisting){
            mUpdatedRssList.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    class CellTowerListener extends PhoneStateListener {
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            processCellInfoList(cellInfo, true);
            //Log.d(TAG, "CellInfoChanged");
        }
    }
}
