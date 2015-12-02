package capurso.io.datacollector.fragments.cellular;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import capurso.io.datacollector.R;

/**
 * Created by cheng on 12/1/15.
 */
public class CellularInfoViewHolder extends RecyclerView.ViewHolder {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_INFO = 1;

    private View mContainer;
    private TextView mTvTowerId, mTvType, mTvRss, mTvTimestamp;
    private int mViewType;

    public CellularInfoViewHolder(View view, int viewType){
        super(view);
        mContainer = view;
        mTvTowerId = (TextView)mContainer.findViewById(R.id.tvTowerId);
        mTvType = (TextView)mContainer.findViewById(R.id.tvCellType);
        mTvRss = (TextView)mContainer.findViewById(R.id.tvRss);
        mTvTimestamp = (TextView)mContainer.findViewById(R.id.tvTimestamp);
        mViewType = viewType;
    }

    public void setTowerId(String towerId){
        mTvTowerId.setText(towerId);
    }

    public void setType(String type){
        mTvType.setText(type);
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
