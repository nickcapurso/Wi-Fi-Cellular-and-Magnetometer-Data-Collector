package capurso.io.datacollector;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import capurso.io.datacollector.fragments.ViewPagerAdapter;
import capurso.io.datacollector.fragments.cellular.CellularFragment;
import capurso.io.datacollector.fragments.magnetic.MagneticFragment;
import capurso.io.datacollector.fragments.wifi.WifiFragment;

public class MainActivity extends AppCompatActivity {
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
}
