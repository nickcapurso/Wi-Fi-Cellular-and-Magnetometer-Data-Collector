package capurso.io.datacollector.fragments;

import android.support.v4.app.Fragment;

public class FragmentWrapper {
    public Fragment fragment;
    public String title;

    public FragmentWrapper(Fragment fragment, String title){
        this.fragment = fragment;
        this.title = title;
    }
}
