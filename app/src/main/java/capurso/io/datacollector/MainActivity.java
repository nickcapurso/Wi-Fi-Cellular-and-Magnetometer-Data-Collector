package capurso.io.datacollector;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import capurso.io.datacollector.common.Utils;
import capurso.io.datacollector.fragments.ViewPagerAdapter;
import capurso.io.datacollector.fragments.cellular.CellularFragment;
import capurso.io.datacollector.fragments.magnetic.MagneticFragment;
import capurso.io.datacollector.fragments.wifi.WifiFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the ActionBar for pre-Lollipop devices
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabLayout = (TabLayout) findViewById(R.id.mainTabs);
        mViewPager = (ViewPager) findViewById(R.id.mainViewPager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new WifiFragment(), getString(R.string.tab_wifi));
        adapter.addFragment(new CellularFragment(), getString(R.string.tab_cellular));
        adapter.addFragment(new MagneticFragment(), getString(R.string.tab_magnetic));
        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SharedPreferences prefs = getSharedPreferences(Utils.PREFS_NAME, 0);
        MenuItem checkedItem = menu.findItem(R.id.action_neverask_filename);
        checkedItem.setChecked(prefs.getBoolean(Utils.PREFS_KEY_NEVERASK_PATH, false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_neverask_filename:
                SharedPreferences prefs = getSharedPreferences(Utils.PREFS_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();

                if(!item.isChecked())
                    //Now, the user is requesting that we always use the default filename
                    editor.putBoolean(Utils.PREFS_KEY_NEVERASK_PATH, true);
                else
                    editor.putBoolean(Utils.PREFS_KEY_NEVERASK_PATH, false);

                editor.apply();
                item.setChecked(!item.isChecked());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
