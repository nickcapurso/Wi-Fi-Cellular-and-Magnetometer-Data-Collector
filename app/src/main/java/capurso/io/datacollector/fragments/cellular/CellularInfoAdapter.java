package capurso.io.datacollector.fragments.cellular;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import capurso.io.datacollector.R;

/**
 * Used with a RecyclerView to display CellularInfo objects.
 */
public class CellularInfoAdapter extends RecyclerView.Adapter<CellularInfoViewHolder> {
    private List<CellularInfo> mItems;
    private Context mContext;

    public CellularInfoAdapter(List<CellularInfo> items, Context context){
        mItems = items;
        mContext = context;
    }

    @Override
    public CellularInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = (LayoutInflater.from(parent.getContext()).inflate(R.layout.view_cellular_row, parent, false));
        return new CellularInfoViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(CellularInfoViewHolder holder, int position) {
        if(holder.getViewType() == CellularInfoViewHolder.TYPE_HEADER){
            //The "fields" of the WifiInfo object will correspond to the header titles in this case
            holder.setTowerId(mContext.getString(R.string.towerid));
            holder.setType(mContext.getString(R.string.type));
            holder.setRss(mContext.getString(R.string.rss));
            holder.setTimestamp(mContext.getString(R.string.timestamp));

        }else if(holder.getViewType() == CellularInfoViewHolder.TYPE_INFO){
            //Get the info object from the list (minus 1 because the item at position 0 is the header)
            CellularInfo info = mItems.get(position - 1);

            //Set data values
            holder.setTowerId(info.towerId);
            holder.setType(info.type);
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
            return CellularInfoViewHolder.TYPE_HEADER;

        return CellularInfoViewHolder.TYPE_INFO;
    }
}
