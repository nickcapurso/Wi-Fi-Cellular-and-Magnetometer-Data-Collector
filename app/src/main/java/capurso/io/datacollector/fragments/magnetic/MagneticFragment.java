package capurso.io.datacollector.fragments.magnetic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import capurso.io.datacollector.R;
import capurso.io.datacollector.SimpleFileDialog;
import capurso.io.datacollector.common.Utils;

/**
 * Registers for magnetometer readings, graphs them, and writes them out to file. Has some similarities
 * to the abstract ScanFragment class, but is different enough not to extend it.
 */
public class MagneticFragment extends Fragment implements View.OnClickListener, SimpleFileDialog.SimpleFileDialogListener, SensorEventListener{
    /**
     * Logcat tag.
     */
    private static final String TAG = MagneticFragment.class.getName();

    /**
     * Reference to the system sensor service.
     */
    private SensorManager mSensorManager;

    /**
     * Reference to the magnetometer sensor type.
     */
    private Sensor mMagnetometer;

    /**
     * Incrementing counter for the graph's X-axis.
     */
    private long mPointCount = 0;

    //aChartEngine graph variables
    private GraphicalView mGraphView;
    private XYSeries mSeries;
    private XYMultipleSeriesDataset mDataset;
    private XYSeriesRenderer mRenderer;
    private XYMultipleSeriesRenderer mMultRenderer;

    /**
     * Reference to the preferences' file.
     */
    private SharedPreferences mPrefs;

    /**
     * Used to print to the output file.
     */
    private PrintWriter mPrinter;

    /**
     * Reference to the Start Sensor button.
     */
    private CardView mBtnScan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        mSensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mBtnScan = (CardView)view.findViewById(R.id.btnStart);
        mBtnScan.setOnClickListener(this);

        setupGraph();

        LinearLayout mMainLayout = (LinearLayout)view.findViewById(R.id.graph);
        mMainLayout.addView(mGraphView);

        mPrefs = getActivity().getSharedPreferences(Utils.PREFS_NAME, 0);

        return view;
    }

    private void setupGraph(){
        mSeries = new XYSeries("Magnetometer");
        mDataset = new XYMultipleSeriesDataset();
        mRenderer = new XYSeriesRenderer();
        mMultRenderer = new XYMultipleSeriesRenderer();

        mDataset.addSeries(mSeries);

        mRenderer.setColor(Color.BLACK);
        mRenderer.setPointStyle(PointStyle.CIRCLE);
        mRenderer.setFillPoints(true);

        mMultRenderer.setXTitle("Time (s)");
        mMultRenderer.setLabelsTextSize(10);
        mMultRenderer.setLabelsColor(Color.BLACK);
        mMultRenderer.setAntialiasing(true);
        mMultRenderer.setYTitle("Values (uT)");
        mMultRenderer.setYAxisMin(0);
        mMultRenderer.setYAxisMax(40);
        mMultRenderer.setPanEnabled(false, false);
        mMultRenderer.setMarginsColor(Color.WHITE);
        mMultRenderer.setAxesColor(Color.parseColor("#707070"));
        mMultRenderer.setShowGrid(true);
        mMultRenderer.setXLabels(3);
        mMultRenderer.addSeriesRenderer(mRenderer);
        mMultRenderer.setLabelsTextSize(60);
        mMultRenderer.setLabelsColor(Color.BLACK);

        mGraphView = ChartFactory.getLineChartView(getContext(), mDataset, mMultRenderer);
    }

    /**
     * Flip the button text, prepare the open/create the output file, and start the magnetometer.
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnStart){
            CardView button = (CardView)v;
            TextView buttonLabel = (TextView)v.findViewById(R.id.tvBtnLabel);

            if(buttonLabel.getText().toString().equals(getString(R.string.start_sensor))){
                //Check if device actually has a magnetometer
                if(mSensorManager == null || mMagnetometer == null){
                    Toast.makeText(getActivity(), getString(R.string.no_magnetometer), Toast.LENGTH_LONG).show();
                    return;
                }

                //Flip button text
                button.setCardBackgroundColor(
                        getResources().getColor(R.color.material_red_500));
                buttonLabel.setText(getString(R.string.stop_sensor));

                //Check preferences to see if we should skip the save file dialog.
                if(!mPrefs.getBoolean(Utils.PREFS_KEY_NEVERASK_PATH, false)){
                    //Show dialog
                    Utils.showSaveFileDialog(getActivity(), Utils.getDefaultFilePath(),
                            Utils.getDefaultFileName(Utils.DATATYPE_MAGNETIC), this);
                }else{
                    //Use default path/file name
                    try{
                        String path = Utils.getDefaultFilePath() + "/" + Utils.getDefaultFileName(Utils.DATATYPE_MAGNETIC);
                        Toast.makeText(getActivity(), getString(R.string.using_default_path) + path, Toast.LENGTH_LONG).show();
                        mPrinter = new PrintWriter(path);
                    } catch (FileNotFoundException e){
                        mPrinter = null;
                    }

                    //If there was an error opening the file.
                    if(mPrinter == null){
                        Toast.makeText(getActivity(), getString(R.string.fileerror), Toast.LENGTH_LONG).show();
                        resetButton();
                        return;
                    }

                    Log.d(TAG, "Starting magnetometer");

                    mSensorManager.registerListener(this, mMagnetometer, Utils.DEFAULT_SAMPLING_RATE);
                }


            }else{
                //Flip button text
                button.setCardBackgroundColor(
                        getResources().getColor(R.color.material_green_500));
                buttonLabel.setText(getString(R.string.start_sensor));

                Log.d(TAG, "Stopping magnetometer");
                mSensorManager.unregisterListener(this);

                //Flush output file and close
                if(mPrinter != null){
                    mPrinter.flush();
                    mPrinter.close();
                    mPrinter = null;
                }
            }
        }
    }

    /**
     * Add new point to the graph and adjust Y axis bounds
     * @param value
     */
    private void displayNewPoint(double value){
        mSeries.add(mPointCount++, value);

        //Moving window adjustment
        if(mSeries.getItemCount() > Utils.MAGNETIC_GRAPH_WINDOW_SIZE)
            mSeries.remove(0);

        mMultRenderer.setYAxisMax(mSeries.getMaxY()+15);
        mMultRenderer.setYAxisMin(mSeries.getMinY() - 15);
        mGraphView.repaint();
    }

    /**
     * Handle new sensor value
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == mMagnetometer){
            double x, y, z, value;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            //Aggregate the three components together
            value = Math.sqrt(x*x+y*y+z*z);
            displayNewPoint(value);

            //Write the new value to file
            if(mPrinter != null)
                mPrinter.write(new StringBuilder(Double.toString(value))
                .append(Utils.FIELD_DELIMITER)
                .append(Utils.getTimestamp())
                .append("\n")
                .toString());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Open or create the chosen file and start the magnetometer.
     * @param chosenDir
     */
    @Override
    public void onChosenDir(String chosenDir) {
        try{
            mPrinter = new PrintWriter(chosenDir);
            Toast.makeText(getActivity(), getString(R.string.using_path) + chosenDir, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            mPrinter = null;
        }
        mSensorManager.registerListener(this, mMagnetometer, Utils.DEFAULT_SAMPLING_RATE);
    }

    @Override
    public void onDialogCanceled() {
        resetButton();
    }

    /**
     * Reset the Start Sensor button in case the dialog was canceled
     */
    private void resetButton(){
        mBtnScan.setCardBackgroundColor(
                getResources().getColor(R.color.material_green_500));

        TextView buttonLabel = (TextView) mBtnScan.findViewById(R.id.tvBtnLabel);
        buttonLabel.setText(getString(R.string.start_sensor));
    }
}
