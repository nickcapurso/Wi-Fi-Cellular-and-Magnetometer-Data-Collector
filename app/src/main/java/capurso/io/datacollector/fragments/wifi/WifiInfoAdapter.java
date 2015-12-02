package capurso.io.datacollector.fragments.wifi;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import capurso.io.datacollector.R;

/**
 * Created by cheng on 12/1/15.
 */
public class WifiInfoAdapter extends RecyclerView.Adapter<WifiInfoViewHolder> {
    private List<WifiInfo> mItems;
    private Context mContext;

    public WifiInfoAdapter(List<WifiInfo> items, Context context){
        mItems = items;
        mContext = context;
    }

    @Override
    public WifiInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = (LayoutInflater.from(parent.getContext()).inflate(R.layout.view_wifi_row, parent, false));
        return new WifiInfoViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(WifiInfoViewHolder holder, int position) {
        if(holder.getViewType() == WifiInfoViewHolder.TYPE_HEADER){
            //The "fields" of the WifiInfo object will correspond to the header titles
            holder.setSsid(mContext.getString(R.string.ssid));
            holder.setMac(mContext.getString(R.string.mac));
            holder.setRss(mContext.getString(R.string.rss));
            holder.setTimestamp(mContext.getString(R.string.timestamp));

        }else if(holder.getViewType() == WifiInfoViewHolder.TYPE_INFO){
            //Get the info object from the list (minus 1 because the item at position 0 is the header)
            WifiInfo info = mItems.get(position - 1);

            holder.setSsid(info.ssid);
            holder.setMac(info.mac);
            holder.setRss(info.rss);
            holder.setTimestamp(info.timestamp);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size()+1; //Include header view
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return WifiInfoViewHolder.TYPE_HEADER;

        return WifiInfoViewHolder.TYPE_INFO;
    }
}
