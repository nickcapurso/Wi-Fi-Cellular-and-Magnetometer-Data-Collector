package capurso.io.datacollector.fragments.wifi;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import capurso.io.datacollector.R;

/**
 * Holds the widgets that comprise a row in a RecyclerView
 */
public class WifiInfoViewHolder extends RecyclerView.ViewHolder {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_INFO = 1;

    private View mContainer;
    private TextView mTvSsid, mTvMac, mTvRss, mTvTimestamp;
    private int mViewType;

    public WifiInfoViewHolder(View view, int viewType){
        super(view);
        mContainer = view;
        mTvSsid = (TextView)mContainer.findViewById(R.id.tvSsid);
        mTvMac = (TextView)mContainer.findViewById(R.id.tvMac);
        mTvRss = (TextView)mContainer.findViewById(R.id.tvRss);
        mTvTimestamp = (TextView)mContainer.findViewById(R.id.tvTimestamp);
        mViewType = viewType;
    }

    public void setSsid(String ssid){
        mTvSsid.setText(ssid);
    }

    public void setMac(String mac){
        mTvMac.setText(mac);
    }

    public void setRss(String rss){
        mTvRss.setText(rss);
    }

    public void setTimestamp(String timestamp){
        mTvTimestamp.setText(timestamp);
    }

    public int getViewType(){
        return mViewType;
    }
}
