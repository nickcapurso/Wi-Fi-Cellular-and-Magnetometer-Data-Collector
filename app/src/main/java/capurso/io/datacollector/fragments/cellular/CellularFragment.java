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
 * Periodically retrieves cell tower information, places the information into a RecyclerView, and
 * writes the information out to file.
 */
public class CellularFragment extends ScanFragment{
    /**
     * LogCat tag.
     */
    private static final String TAG = CellularFragment.class.getName();

    /**
     * Displays cell tower information in a table-like format.
     */
    private RecyclerView mRvScanResults;

    /**
     * Adapter for the RecyclerView.
     */
    private CellularInfoAdapter mAdapter;

    /**
     * Contains all current information being displayed in the RecyclerView (and was just written
     * out to file).
     */
    private List<CellularInfo> mCellularInfos;

    /**
     * Contains updated RSS information within the last scan interval. Used to update
     * the information in mCellularInfos before they are displayed in the RecyclerView.
     */
    private List<CellularInfo> mUpdatedRssList;

    /**
     * Used to register for listening for cell tower information.
     */
    private TelephonyManager mTelephonyManager;

    /**
     * Receives callbacks from the OS containing cell tower information.
     */
    private CellTowerListener mTowerListener;

    /**
     * Writes cell tower information to file.
     */
    private PrintWriter mPrinter;

    /**
     * Used to periodically start cell tower data retrieval.
     */
    private Timer mScanTimer = null;

    /**
     * Keeps track of the current interval - the RecyclerView is cleared on every 8th interval.
     */
    private int mIntervalCount = 0;

    /**
     * Post changes to the UI.
     */
    private Handler mHandler;

    /**
     * General object used for synchronizing access to mCellularInfos
     */
    private Object mLock;

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

        //Set up variables for cell tower information collection
        mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        mTowerListener = new CellTowerListener();

        mHandler = new Handler();

        mLock = new Object();
        return view;
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

        mCellularInfos.clear();
        mAdapter.notifyDataSetChanged();
        mPrinter = outputFile;

        //Scheduling data collection events at periodic intervals (default = 3 seconds).
        mScanTimer = new Timer();
        mScanTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Changes to the view hierarchy can only be made on the thread that created them,
                //post the changes to the UI thread.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        processCellInfoList(mTelephonyManager.getAllCellInfo(), false);
                    }
                });

                //Reset the RecyclerView periodically.
                if(mIntervalCount == Utils.CELL_LIST_CLEAR_INTERVAL){
                    mIntervalCount = 0;

                    //Need synchronized access to mCellularInfos because it may be modified
                    //(by processCellInfoList) while it is being cleared.
                    synchronized(mLock) {
                        mCellularInfos.clear();
                    }
                }

                mIntervalCount++;
            }
        }, 0, Utils.DEFAULT_SCAN_INTERVAL);

        //Also listen for updated cell info events
        Log.d(TAG, "Starting to listen for cell info");
        mTelephonyManager.listen(
                mTowerListener,
                PhoneStateListener.LISTEN_CELL_INFO);

    }

    /**
     * Cancel periodic data collection and close the opened file.
     */
    @Override
    protected void stopScanning() {
        Log.d(TAG, "Stopping listening for cell info");

        //Unregister listening for updated cell info events
        mTelephonyManager.listen(
                mTowerListener,
                PhoneStateListener.LISTEN_NONE);

        if(mScanTimer != null) {
            //Cancel periodic data collection
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

    @Override
    protected boolean readyToScan() {
        return true;
    }

    @Override
    protected String getDataType() {
        return Utils.DATATYPE_CELLULAR;
    }

    /**
     * Retrieves the tower ID, network type, RSS, and timestamp for a CDMA cell.
     * @param info
     * @return
     */
    private CellularInfo processCdmaInfo(CellInfoCdma info){
        int id = info.getCellIdentity().getBasestationId();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_CDMA;

        //RSS can not always be obtained
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    /**
     * Retrieves the tower ID, network type, RSS, and timestamp for a GSM cell.
     * @param info
     * @return
     */
    private CellularInfo processGsmInfo(CellInfoGsm info){
        int id = info.getCellIdentity().getCid();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_GSM;

        //RSS can not always be obtained
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    /**
     * Retrieves the tower ID, network type, RSS, and timestamp for an LTE cell.
     * @param info
     * @return
     */
    private CellularInfo processLteInfo(CellInfoLte info){
        int id = info.getCellIdentity().getCi();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_LTE;

        //RSS can not always be obtained
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    /**
     * Retrieves the tower ID, network type, RSS, and timestamp for a WCDMA cell.
     * @param info
     * @return
     */
    private CellularInfo processWcdmaInfo(CellInfoWcdma info){
        int id = info.getCellIdentity().getCid();
        int asu = info.getCellSignalStrength().getAsuLevel();
        int rss = info.getCellSignalStrength().getDbm();
        String type = CellularInfo.TYPE_WCDMA;

        //RSS can not always be obtained
        String strRss = (asu == 99 || rss > 100 || rss < -200)? "N/A" : Integer.toString(rss);

        if(id < 0)
            return null;

        return new CellularInfo(Integer.toString(id), type, strRss, Utils.getTimestamp());
    }

    /**
     * Given a list of CellInfo objects (information about surrounding cells), add each one
     * to the RecyclerView and write them out to file.
     * @param infoList
     * @param updateExisting
     */
    private void processCellInfoList(List<CellInfo> infoList, boolean updateExisting){
        int existingIndex = 0;
        if(infoList == null || infoList.size() == 0)
            return;

        for (CellInfo i : infoList) {
            CellularInfo newInfo = null;

            //Call the correct function based on the type of cell
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
                //Keep track of collected RSS readings within this scanning interval, with
                //the intention of updating mCellularInfos before the information is displayed/written to file.
                existingIndex = mUpdatedRssList.indexOf(newInfo);

                if(existingIndex != -1)
                    mUpdatedRssList.set(existingIndex, newInfo);
                else
                    mUpdatedRssList.add(newInfo);
            }else{
                existingIndex = mUpdatedRssList.indexOf(newInfo);
                if(existingIndex != -1) {
                    //If the current cell is already in the updated RSS list, use that one instead.
                    CellularInfo existingInfo = mUpdatedRssList.get(existingIndex);

                    //Add the existing information to the list to be displayed in the RecyclerView
                    synchronized (mLock) {
                        mCellularInfos.add(existingInfo);
                    }

                    //Write the information to file
                    if(mPrinter !=  null) {
                        String s = existingInfo.toString();
                        mPrinter.print(s);
                        Log.d(TAG, s);
                    }
                }else {
                    //Add the new information to the list to be displayed in the RecyclerView
                    synchronized (mLock) {
                        mCellularInfos.add(newInfo);
                    }

                    //Write the information to file
                    if(mPrinter !=  null) {
                        String s = newInfo.toString();
                        mPrinter.print(s);
                        Log.d(TAG, s);
                    }
                }
            }
        }

        //Clear the updated RSS list at the end of the scanning interval
        if(!updateExisting){
            mUpdatedRssList.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Receives callbacks when cell information changes
     */
    class CellTowerListener extends PhoneStateListener {
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            processCellInfoList(cellInfo, true);
        }
    }
}
