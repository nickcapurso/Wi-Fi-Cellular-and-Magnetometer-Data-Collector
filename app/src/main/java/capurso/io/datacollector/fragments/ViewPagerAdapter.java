package capurso.io.datacollector.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<FragmentWrapper> mFragmentList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager){
        super(manager);
    }

    public void addFragment(Fragment fragment, String title){
        mFragmentList.add(new FragmentWrapper(fragment, title));
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position).fragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentList.get(position).title;
    }
}
