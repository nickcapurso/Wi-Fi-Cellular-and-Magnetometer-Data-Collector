package capurso.io.datacollector.fragments;

import android.support.v4.app.Fragment;

/**
 * Wraps a fragment along with its tab title.
 */
public class FragmentWrapper {
    public Fragment fragment;
    public String title;

    public FragmentWrapper(Fragment fragment, String title){
        this.fragment = fragment;
        this.title = title;
    }
}
