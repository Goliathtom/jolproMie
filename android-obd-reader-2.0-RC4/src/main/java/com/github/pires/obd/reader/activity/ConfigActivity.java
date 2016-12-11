package com.github.pires.obd.reader.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.SystemOfUnits;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.enums.AvailableCommandNames;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.reader.R;
import com.github.pires.obd.reader.config.ObdConfig;
import com.github.pires.obd.reader.io.AbstractGatewayService;
import com.github.pires.obd.reader.io.LogCSVWriter;
import com.github.pires.obd.reader.io.MockObdGatewayService;
import com.github.pires.obd.reader.io.ObdCommandJob;
import com.github.pires.obd.reader.io.ObdGatewayService;
import com.github.pires.obd.reader.io.ObdProgressListener;
import com.github.pires.obd.reader.net.ObdReading;
import com.github.pires.obd.reader.net.ObdService;
import com.github.pires.obd.reader.trips.TripLog;
import com.github.pires.obd.reader.trips.TripRecord;

import managing.ManagingPoint;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import managing.points.PointLog;
import managing.points.PointRecord;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


/**
 * Configuration com.github.pires.obd.reader.activity.
 */
public class ConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
    public static final String UPLOAD_URL_KEY = "upload_url_preference";
    public static final String UPLOAD_DATA_KEY = "upload_data_preference";
    public static final String OBD_UPDATE_PERIOD_KEY = "obd_update_period_preference";
    public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
    public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
    public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";
    public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
    public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
    public static final String PROTOCOLS_LIST_KEY = "obd_protocols_preference";
    public static final String ENABLE_GPS_KEY = "enable_gps_preference";
    public static final String GPS_UPDATE_PERIOD_KEY = "gps_update_period_preference";
    public static final String GPS_DISTANCE_PERIOD_KEY = "gps_distance_period_preference";
    public static final String ENABLE_BT_KEY = "enable_bluetooth_preference";
    public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference";
    public static final String CONFIG_READER_KEY = "reader_config_preference";
    public static final String ENABLE_FULL_LOGGING_KEY = "enable_full_logging";
    public static final String DIRECTORY_FULL_LOGGING_KEY = "directory_full_logging";
    public static final String DEV_EMAIL_KEY = "dev_email";

    static boolean tmap_flag = false;
    //private int remainFuel_pb = 0;
    /**
     * @param prefs
     * @return
     */
    public static int getObdUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.
                getString(ConfigActivity.OBD_UPDATE_PERIOD_KEY, "1"); // 1 as in seconds
        int period = 1000; // by default 4000ms

        try {
            period = (int) (Double.parseDouble(periodString) * 1000);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 1000;
        }

        return period;
    }

    /**
     * @param prefs
     * @return
     */
    public static double getVolumetricEfficieny(SharedPreferences prefs) {
        String veString = prefs.getString(ConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
        double ve = 0.85;
        try {
            ve = Double.parseDouble(veString);
        } catch (Exception e) {
        }
        return ve;
    }

    /**
     * @param prefs
     * @return
     */
    public static double getEngineDisplacement(SharedPreferences prefs) {
        String edString = prefs.getString(ConfigActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
        double ed = 1.6;
        try {
            ed = Double.parseDouble(edString);
        } catch (Exception e) {
        }
        return ed;
    }

    /**
     * @param prefs
     * @return
     */
    public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        ArrayList<ObdCommand> ucmds = new ArrayList<>();
        for (int i = 0; i < cmds.size(); i++) {
            ObdCommand cmd = cmds.get(i);
            boolean selected = prefs.getBoolean(cmd.getName(), true);
            if (selected)
                ucmds.add(cmd);
        }
        return ucmds;
    }

    /**
     * @param prefs
     * @return
     */
    public static double getMaxFuelEconomy(SharedPreferences prefs) {
        String maxStr = prefs.getString(ConfigActivity.MAX_FUEL_ECON_KEY, "70");
        double max = 70;
        try {
            max = Double.parseDouble(maxStr);
        } catch (Exception e) {
        }
        return max;
    }

    /**
     * @param prefs
     * @return
     */
    public static String[] getReaderConfigCommands(SharedPreferences prefs) {
        String cmdsStr = prefs.getString(CONFIG_READER_KEY, "atsp0\natz");
        String[] cmds = cmdsStr.split("\n");
        return cmds;
    }

    /**
     * Minimum time between location updates, in milliseconds
     *
     * @param prefs
     * @return
     */
    public static int getGpsUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs
                .getString(ConfigActivity.GPS_UPDATE_PERIOD_KEY, "1"); // 1 as in seconds
        int period = 1000; // by default 1000ms

        try {
            period = (int) (Double.parseDouble(periodString) * 1000);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 1000;
        }

        return period;
    }

    /**
     * Min Distance between location updates, in meters
     *
     * @param prefs
     * @return
     */
    public static float getGpsDistanceUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs
                .getString(ConfigActivity.GPS_DISTANCE_PERIOD_KEY, "1"); // 5 as in meters
        float period = 1; // by default 5 meters

        try {
            period = Float.parseFloat(periodString);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 1;
        }

        return period;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    /*
     * Read preferences resources available at res/xml/preferences.xml
     */
        addPreferencesFromResource(R.xml.preferences);

        checkGps();

        ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<>();
        ArrayList<CharSequence> vals = new ArrayList<>();
        ListPreference listBtDevices = (ListPreference) getPreferenceScreen()
                .findPreference(BLUETOOTH_LIST_KEY);
        ArrayList<CharSequence> protocolStrings = new ArrayList<>();
        ListPreference listProtocols = (ListPreference) getPreferenceScreen()
                .findPreference(PROTOCOLS_LIST_KEY);
        String[] prefKeys = new String[]{ENGINE_DISPLACEMENT_KEY,
                VOLUMETRIC_EFFICIENCY_KEY, OBD_UPDATE_PERIOD_KEY, MAX_FUEL_ECON_KEY};
        for (String prefKey : prefKeys) {
            EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen()
                    .findPreference(prefKey);
            txtPref.setOnPreferenceChangeListener(this);
        }

    /*
     * Available OBD commands
     *
     * TODO This should be read from preferences database
     */
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen()
                .findPreference(COMMANDS_SCREEN_KEY);
        for (ObdCommand cmd : cmds) {
            CheckBoxPreference cpref = new CheckBoxPreference(this);
            cpref.setTitle(cmd.getName());
            cpref.setKey(cmd.getName());
            cpref.setChecked(true);
            cmdScr.addPreference(cpref);
        }

        //final DistanceMILOnCommand kmcommand = (DistanceMILOnCommand) ObdCommandJob.getCommand();
        //int current_distance = kmcommand.getKm() % 2000;
    /*
     * Available OBD protocols
     *
     */
        for (ObdProtocols protocol : ObdProtocols.values()) {
            protocolStrings.add(protocol.name());
        }
        listProtocols.setEntries(protocolStrings.toArray(new CharSequence[0]));
        listProtocols.setEntryValues(protocolStrings.toArray(new CharSequence[0]));

    /*
     * Let's use this device Bluetooth adapter to select which paired OBD-II
     * compliant device we'll use.
     */
        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            listBtDevices
                    .setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));

            // we shouldn't get here, still warn user
            Toast.makeText(this, "This device does not support Bluetooth.",
                    Toast.LENGTH_LONG).show();

            return;
        }

    /*
     * Listen for preferences click.
     *
     * TODO there are so many repeated validations :-/
     */
        final Activity thisActivity = this;
        listBtDevices.setEntries(new CharSequence[1]);
        listBtDevices.setEntryValues(new CharSequence[1]);
        listBtDevices.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // see what I mean in the previous comment?
                if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
                    Toast.makeText(thisActivity,
                            "This device does not support Bluetooth or it is disabled.",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

    /*
     * Get paired devices and populate preference list.
     */
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                vals.add(device.getAddress());
            }
        }
        listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
    }

    /**
     * OnPreferenceChangeListener method that will validate a preferencen new
     * value when it's changed.
     *
     * @param preference the changed preference
     * @param newValue   the value to be validated and set if valid
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (OBD_UPDATE_PERIOD_KEY.equals(preference.getKey())
                || VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey())
                || ENGINE_DISPLACEMENT_KEY.equals(preference.getKey())
                || MAX_FUEL_ECON_KEY.equals(preference.getKey())
                || GPS_UPDATE_PERIOD_KEY.equals(preference.getKey())
                || GPS_DISTANCE_PERIOD_KEY.equals(preference.getKey())) {
            try {
                Double.parseDouble(newValue.toString().replace(",", "."));
                return true;
            } catch (Exception e) {
                Toast.makeText(this,
                        "Couldn't parse '" + newValue.toString() + "' as a number.",
                        Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    private void checkGps() {
        LocationManager mLocService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocService != null) {
            LocationProvider mLocProvider = mLocService.getProvider(LocationManager.GPS_PROVIDER);
            if (mLocProvider == null) {
                hideGPSCategory();
            }
        }
    }

    private void hideGPSCategory() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getResources().getString(R.string.pref_gps_category));
        if (preferenceCategory != null) {
            preferenceCategory.removeAll();
            preferenceScreen.removePreference((Preference) preferenceCategory);
        }
    }

    @ContentView(R.layout.main)
    public static class MainActivity extends RoboActivity implements ObdProgressListener, LocationListener, GpsStatus.Listener {

        private static final String TAG = MainActivity.class.getName();
        private static final int NO_BLUETOOTH_ID = 0;
        private static final int BLUETOOTH_DISABLED = 1;
        private static final int START_LIVE_DATA = 2;
        private static final int STOP_LIVE_DATA = 3;
        private static final int SETTINGS = 4;
        private static final int GET_DTC = 5;
        private static final int TABLE_ROW_MARGIN = 7;
        private static final int NO_ORIENTATION_SENSOR = 8;
        private static final int NO_GPS_SUPPORT = 9;
        private static final int TRIPS_LIST = 10;
        private static final int SAVE_TRIP_NOT_AVAILABLE = 11;

        //
        private static  final int TMAP = 98;
        private static final int POINT_LIST = 99;
        private static boolean bluetoothDefaultIsEnable = false;

        static {
            RoboGuice.setUseAnnotationDatabases(false);
        }

        public Map<String, String> commandResult = new HashMap<String, String>();
        boolean mGpsIsStarted = false;
        private LocationManager mLocService;
        private LocationProvider mLocProvider;
        private LogCSVWriter myCSVWriter;
        private Location mLastLocation;

        /// Managing Point
        private ManagingPoint managingPoint = new ManagingPoint();
        private DistanceMILOnCommand distanceMILOnCommand = new DistanceMILOnCommand();
        private Random random = new Random();
        /// the trip log
        private TripLog triplog;
        private TripRecord currentTrip;

        // the Point Log
        private PointLog pointlog;
        private PointRecord pointrecord;

        private Context context;
        @InjectView(R.id.compass_text)
        private TextView compass;
        private final SensorEventListener orientListener = new SensorEventListener() {

            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                String dir = "";
                if (x >= 337.5 || x < 22.5) {
                    dir = "N";
                } else if (x >= 22.5 && x < 67.5) {
                    dir = "NE";
                } else if (x >= 67.5 && x < 112.5) {
                    dir = "E";
                } else if (x >= 112.5 && x < 157.5) {
                    dir = "SE";
                } else if (x >= 157.5 && x < 202.5) {
                    dir = "S";
                } else if (x >= 202.5 && x < 247.5) {
                    dir = "SW";
                } else if (x >= 247.5 && x < 292.5) {
                    dir = "W";
                } else if (x >= 292.5 && x < 337.5) {
                    dir = "NW";
                }
                updateTextView(compass, dir);
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // do nothing
            }
        };
        @InjectView(R.id.BT_STATUS)
        private TextView btStatusTextView;
        @InjectView(R.id.OBD_STATUS)
        private TextView obdStatusTextView;
        @InjectView(R.id.GPS_POS)
        private TextView gpsStatusTextView;
        @InjectView(R.id.vehicle_view)
        private LinearLayout vv;
        @InjectView(R.id.data_table)
        private TableLayout tl;
        @InjectView(R.id.remain_dt)
        private TextView remain_dt_data;
        @InjectView(R.id.cpoint_txt)
        private TextView view_c_point;
        @Inject
        private SensorManager sensorManager;
        @Inject
        private PowerManager powerManager;
        @Inject
        private SharedPreferences prefs;
        private boolean isServiceBound;
        private AbstractGatewayService service;
        private final Runnable mQueueCommands = new Runnable() {
            public void run() {
                if (service != null && service.isRunning() && service.queueEmpty()) {
                    queueCommands();

                    double lat = 0;
                    double lon = 0;
                    double alt = 0;
                    final int posLen = 7;
                    if (mGpsIsStarted && mLastLocation != null) {
                        lat = mLastLocation.getLatitude();
                        lon = mLastLocation.getLongitude();
                        alt = mLastLocation.getAltitude();

                        StringBuilder sb = new StringBuilder();
                        sb.append("Lat: ");
                        sb.append(String.valueOf(mLastLocation.getLatitude()).substring(0, posLen));
                        sb.append(" Lon: ");
                        sb.append(String.valueOf(mLastLocation.getLongitude()).substring(0, posLen));
                        sb.append(" Alt: ");
                        sb.append(String.valueOf(mLastLocation.getAltitude()));
                        gpsStatusTextView.setText(sb.toString());
                    }
                    if (prefs.getBoolean(UPLOAD_DATA_KEY, false)) {
                        // Upload the current reading by http
                        final String vin = prefs.getString(VEHICLE_ID_KEY, "UNDEFINED_VIN");
                        Map<String, String> temp = new HashMap<String, String>();
                        temp.putAll(commandResult);
                        ObdReading reading = new ObdReading(lat, lon, alt, System.currentTimeMillis(), vin, temp);
                        new UploadAsyncTask().execute(reading);

                    } else if (prefs.getBoolean(ENABLE_FULL_LOGGING_KEY, false)) {
                        // Write the current reading to CSV
                        final String vin = prefs.getString(VEHICLE_ID_KEY, "UNDEFINED_VIN");
                        Map<String, String> temp = new HashMap<String, String>();
                        temp.putAll(commandResult);
                        ObdReading reading = new ObdReading(lat, lon, alt, System.currentTimeMillis(), vin, temp);
                        myCSVWriter.writeLineCSV(reading);
                    }
                    commandResult.clear();
                }
                // run again in period defined in preferences
                new Handler().postDelayed(mQueueCommands, getObdUpdatePeriod(prefs));
            }
        };
        private Sensor orientSensor = null;
        private PowerManager.WakeLock wakeLock = null;
        private boolean preRequisites = true;
        private ServiceConnection serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder binder) {
                Log.d(TAG, className.toString() + " service is bound");
                isServiceBound = true;
                service = ((AbstractGatewayService.AbstractGatewayServiceBinder) binder).getService();
                service.setContext(MainActivity.this);
                Log.d(TAG, "Starting live data");
                try {
                    service.startService();
                    if (preRequisites)
                        btStatusTextView.setText(getString(R.string.status_bluetooth_connected));
                } catch (IOException ioe) {
                    Log.e(TAG, "Failure Starting live data");
                    btStatusTextView.setText(getString(R.string.status_bluetooth_error_connecting));
                    doUnbindService();
                }
            }

            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }

            // This method is *only* called when the connection to the service is lost unexpectedly
            // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
            // So the isServiceBound attribute should also be set to false when we unbind from the service.
            @Override
            public void onServiceDisconnected(ComponentName className) {
                Log.d(TAG, className.toString() + " service is unbound");
                isServiceBound = false;
            }
        };

        public MainActivity() {
        }

        public static String LookUpCommand(String txt) {
            for (AvailableCommandNames item : AvailableCommandNames.values()) {
                if (item.getValue().equals(txt)) return item.name();
            }
            return txt;
        }

        public void updateTextView(final TextView view, final String txt) {
            new Handler().post(new Runnable() {
                public void run() {
                    view.setText(txt);
                }
            });
        }

        public void stateUpdate(final ObdCommandJob job) {
            final String cmdName = job.getCommand().getName();
            String cmdResult = "";
            final String cmdID = LookUpCommand(cmdName);

            Log.d(TAG, "CMD ID : "+cmdID);
            if (job.getState().equals(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR)) {
                cmdResult = job.getCommand().getResult();
                if (cmdResult != null) {
                    obdStatusTextView.setText(cmdResult.toLowerCase());
                }
            } else if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NOT_SUPPORTED)) {
                cmdResult = getString(R.string.status_obd_no_support);
            } else {
                cmdResult = job.getCommand().getFormattedResult();
                obdStatusTextView.setText(getString(R.string.status_obd_data));
            }

            if (vv.findViewWithTag(cmdID) != null) {
                TextView existingTV = (TextView) vv.findViewWithTag(cmdID);
                existingTV.setText(cmdResult);
            } else addTableRow(cmdID, cmdName, cmdResult);
               commandResult.put(cmdID, cmdResult);
            // 스피드에 대한 Progress Bar
            if(cmdID.equals(AvailableCommandNames.SPEED.toString())){
                String[] kmdata = cmdResult.split("km");
                ProgressBar pb_speed = (ProgressBar) findViewById((R.id.speed_pb));
                pb_speed.setProgress(Integer.parseInt(kmdata[0]));

                // 현재 차량 속도
                //int current_speed = Integer.parseInt(kmdata[0]);
                int current_speed = 100 + random.nextInt(200); // The Function Test with the Random values
                // Notice for over-speed
                if(current_speed>180){
                    // we shouldn't get here, still warn user
                    Toast.makeText(this, "Too Much Fast! Reduce the Speed!",
                            Toast.LENGTH_LONG).show();
                }
                int current_distance = (current_speed*1000) / 900; // Distance drived per each 1 seconds
                distanceMILOnCommand.setCurrentDrivingDistance(distanceMILOnCommand.getKm()+current_distance);
/*            }
            // 이동 거리-포인트
            if(cmdID.equals(AvailableCommandNames.DISTANCE_TRAVELED_ON.toString())){*/
                TextView dtv = (TextView) findViewById(R.id.distance_text);
                ProgressBar pb_distance = (ProgressBar) findViewById((R.id.distance_pb));

/*                // Split Data of Km into km to get Int value of KM
                String[] kmdata = cmdResult.split("km");*/

                // Write down the distance on textview under 'distance_text'
                dtv.setText(String.format("%d%s", distanceMILOnCommand.getKm(), "m"));
                // Draw circle progress bar with m data.
                pb_distance.setProgress(distanceMILOnCommand.getKm()%1000);

                int remain_dt_pb = 1000 - (distanceMILOnCommand.getKm()%1000); // km that is remained to get points
                remain_dt_data.setText(String.format("%d%s", remain_dt_pb, "m")); // setText of remained km
                ProgressBar pb_point = (ProgressBar) findViewById(R.id.pb_point); // progressBar value
                pb_point.setProgress(remain_dt_pb);
                // Managing Points
                int __point_count = distanceMILOnCommand.getKm() / 1000;
                //int __point_count = 16 / 5;
                if(managingPoint != null){
                    if(__point_count != 0){
                        managingPoint.setPointCount(__point_count);
                        int current_point = managingPoint.getPointCount() * 20; // 20 Points per each 1 KM
                        managingPoint.setPoint(current_point);
                        view_c_point.setText(String.valueOf(current_point)); // Point : Int -> String
                    }
                }
/*            }
            // 시간당 연료 소비량에 대한 text 와 progressBar
            if(cmdID.equals(AvailableCommandNames.FUEL_CONSUMPTION_RATE.toString())){*/

              //      String[] fueldata = cmdResult.split("L");
                    ProgressBar fuel_pb = (ProgressBar) findViewById((R.id.fuel_pb));
                    TextView frtv = (TextView) findViewById(R.id.fuel_rate_text);
                    //int remainFuel_pb = 800000 - distanceMILOnCommand.getKm();
                    int remainFuel_pb = 81111 - distanceMILOnCommand.getKm();

                // 평균 연비는 8000m/L로 설정.
                  //  int consumedfuel = distanceMILOnCommand.getKm() / 8; : 누적 이동 거리에 대한 연료소비량
                    fuel_pb.setProgress(remainFuel_pb );

                    // 남은 연료로 주행가능 거리가 80KM 미만일 때,
                    if(remainFuel_pb<80000){
                        if(!tmap_flag) {
                            // we shouldn't get here, still warn user
                            tmap_flag = true;
                            Toast.makeText(this, "Lack of Fuel! Please Go to Charge!",
                                    Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, TmapActivity.class);
                            intent.putExtra("Warning_Fuel", "true");
                            startActivity(intent);
                        }
                    }
                    frtv.setText(String.format("%d%s", ( remainFuel_pb / 1000), "KM"));
            }
            // 연료 타입에 대한 text
            if(cmdID.equals(AvailableCommandNames.FUEL_TYPE.toString())){
                TextView ftv = (TextView) findViewById(R.id.fueltype_text);
                ftv.setText("Diesel");
            }
            if(cmdID.equals(AvailableCommandNames.ENGINE_RPM.toString())){
                String[] rpmdata = cmdResult.split("RPM");
                ProgressBar engine_RPM_pb = (ProgressBar) findViewById((R.id.engine_RPM_pb));
                engine_RPM_pb.setProgress(Integer.parseInt(rpmdata[0]));
                if(Integer.parseInt(rpmdata[0])>5000){
                    // we shouldn't get here, still warn user
                    Toast.makeText(this, "Too Much PRM to Work Engine! Too Dangerous!",
                            Toast.LENGTH_LONG).show();
                }
            }
            if(cmdID.equals(AvailableCommandNames.ENGINE_LOAD.toString())) {
                TextView rpmtv = (TextView) findViewById(R.id.engine_load_text);
                rpmtv.setText(cmdResult);
            }
            if(cmdID.equals(AvailableCommandNames.ENGINE_COOLANT_TEMP.toString())){
                TextView cool_ttv = (TextView) findViewById(R.id.engine_cool_temp_text);
                cool_ttv.setText(cmdResult);
            }

            updateTripStatistic(job, cmdID);
        }

        private boolean gpsInit() {
            mLocService = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (mLocService != null) {
                mLocProvider = mLocService.getProvider(LocationManager.GPS_PROVIDER);
                if (mLocProvider != null) {
                    mLocService.addGpsStatusListener(this);
                    if (mLocService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        gpsStatusTextView.setText(getString(R.string.status_gps_ready));
                        return true;
                    }
                }
            }
            gpsStatusTextView.setText(getString(R.string.status_gps_no_support));
            showDialog(NO_GPS_SUPPORT);
            Log.e(TAG, "Unable to get GPS PROVIDER");
            // todo disable gps controls into Preferences
            return false;
        }

        private void updateTripStatistic(final ObdCommandJob job, final String cmdID) {

            if (currentTrip != null) {
                if (cmdID.equals(AvailableCommandNames.SPEED.toString())) {
                    SpeedCommand command = (SpeedCommand) job.getCommand();
                    currentTrip.setSpeedMax(command.getMetricSpeed());
                } else if (cmdID.equals(AvailableCommandNames.ENGINE_RPM.toString())) {
                    RPMCommand command = (RPMCommand) job.getCommand();
                    currentTrip.setEngineRpmMax(command.getRPM());
                } else if (cmdID.endsWith(AvailableCommandNames.ENGINE_RUNTIME.toString())) {
                    RuntimeCommand command = (RuntimeCommand) job.getCommand();
                    currentTrip.setEngineRuntime(command.getFormattedResult());
                } else if (cmdID.equals(AvailableCommandNames.DISTANCE_TRAVELED_ON.toString())){
                    DistanceMILOnCommand command = (DistanceMILOnCommand) job.getCommand();
                    currentTrip.setTraveledDistance(String.format("%d%s", distanceMILOnCommand.getKm(), "m"));
                    currentTrip.setSavePoint(managingPoint.getPoint());
                }
            }

            if(pointrecord != null){
                pointrecord.setspentPoint(0);
                pointrecord.setgotPoint(managingPoint.getPoint());
                pointrecord.setcurrentPoint(pointrecord.calccurrentPoint());
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null)
                bluetoothDefaultIsEnable = btAdapter.isEnabled();

            // get Orientation sensor
            List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
            if (sensors.size() > 0)
                orientSensor = sensors.get(0);
            else
                showDialog(NO_ORIENTATION_SENSOR);


            context = this.getApplicationContext();
            // create a log instance for use by this application
            triplog = TripLog.getInstance(context);
            // create a log instance for use by this application
            pointlog = PointLog.getInstance(context);
        }

        @Override
        protected void onStart() {
            super.onStart();
            Log.d(TAG, "Entered onStart...");
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            if (mLocService != null) {
                mLocService.removeGpsStatusListener(this);
                mLocService.removeUpdates(this);
            }

            releaseWakeLockIfHeld();
            if (isServiceBound) {
                doUnbindService();
            }

            endTrip();

            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null && btAdapter.isEnabled() && !bluetoothDefaultIsEnable)
                btAdapter.disable();
        }

        @Override
        protected void onPause() {
            super.onPause();
            Log.d(TAG, "Pausing..");
            releaseWakeLockIfHeld();
        }

        /**
         * If lock is held, release. Lock will be held when the service is running.
         */
        private void releaseWakeLockIfHeld() {
            if (wakeLock.isHeld())
                wakeLock.release();
        }

        protected void onResume() {
            super.onResume();
            Log.d(TAG, "Resuming..");
            sensorManager.registerListener(orientListener, orientSensor,
                    SensorManager.SENSOR_DELAY_UI);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "ObdReader");

            // get Bluetooth device
            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            preRequisites = btAdapter != null && btAdapter.isEnabled();
            if (!preRequisites && prefs.getBoolean(ENABLE_BT_KEY, false)) {
                preRequisites = btAdapter.enable();
            }

            gpsInit();

            if (!preRequisites) {
                showDialog(BLUETOOTH_DISABLED);
                btStatusTextView.setText(getString(R.string.status_bluetooth_disabled));
            } else {
                btStatusTextView.setText(getString(R.string.status_bluetooth_ok));
            }
        }

        private void updateConfig() {
            startActivity(new Intent(this, ConfigActivity.class));
        }

        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, START_LIVE_DATA, 0, getString(R.string.menu_start_live_data));
            menu.add(0, STOP_LIVE_DATA, 0, getString(R.string.menu_stop_live_data));
            menu.add(0, GET_DTC, 0, getString(R.string.menu_get_dtc));
            menu.add(0, TRIPS_LIST, 0, getString(R.string.menu_trip_list));
            menu.add(0, POINT_LIST, 0, "Point List");
            menu.add(0, TMAP, 0, "MiE Map");
            menu.add(0, SETTINGS, 0, getString(R.string.menu_settings));
            return true;
        }

        // private void staticCommand() {
        // Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
        // startActivity(commandIntent);
        // }

        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case START_LIVE_DATA:
                    startLiveData();
                    return true;
                case STOP_LIVE_DATA:
                    stopLiveData();
                    return true;
                case SETTINGS:
                    updateConfig();
                    return true;
                case GET_DTC:
                    getTroubleCodes();
                    return true;
                case TRIPS_LIST:
                    startActivity(new Intent(this, TripListActivity.class));
                    return true;
                case POINT_LIST:
                    startActivity(new Intent(this, PointListActivity.class));
                    return true;
                case TMAP:
                    startActivity(new Intent(this, TmapActivity.class));
                    return true;
                // case COMMAND_ACTIVITY:
                // staticCommand();
                // return true;
            }
            return false;
        }

        private void getTroubleCodes() {
            startActivity(new Intent(this, TroubleCodesActivity.class));
        }

        private void startLiveData() {
            Log.d(TAG, "Starting live data..");

            tl.removeAllViews(); //start fresh
            distanceMILOnCommand.setCurrentDrivingDistance(0);
            managingPoint.setPointCount(0); // Initialize the Counter of Points Gathered
            managingPoint.setPoint(0);
            doBindService();

            currentTrip = triplog.startTrip();
            pointrecord = pointlog.startTrip();

            if (currentTrip == null)
                showDialog(SAVE_TRIP_NOT_AVAILABLE);
            if (pointrecord == null)
                showDialog(SAVE_TRIP_NOT_AVAILABLE);

            // start command execution
            new Handler().post(mQueueCommands);

            if (prefs.getBoolean(ENABLE_GPS_KEY, false))
                gpsStart();
            else
                gpsStatusTextView.setText(getString(R.string.status_gps_not_used));

            // screen won't turn off until wakeLock.release()
            wakeLock.acquire();

            if (prefs.getBoolean(ENABLE_FULL_LOGGING_KEY, false)) {

                // Create the CSV Logger
                long mils = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss");

                myCSVWriter = new LogCSVWriter("Log" + sdf.format(new Date(mils)).toString() + ".csv",
                        prefs.getString(DIRECTORY_FULL_LOGGING_KEY,
                                getString(R.string.default_dirname_full_logging))
                );
            }
        }

        private void stopLiveData() {
            Log.d(TAG, "Stopping live data..");

            gpsStop();

            doUnbindService();
            endTrip();

            //최종 거리, 최종 포인트에 대해 DB에 정보저장

            releaseWakeLockIfHeld();
    final String devemail = prefs.getString(DEV_EMAIL_KEY,null);
            if (devemail != null) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                ObdGatewayService.saveLogcatToFile(getApplicationContext(), devemail);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Where there issues?\nThen please send us the logs.\nSend Logs?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }

            if (myCSVWriter != null) {
                myCSVWriter.closeLogCSVWriter();
            }
        }

        protected void endTrip() {
            if (currentTrip != null) {
                currentTrip.setEndDate(new Date());
                triplog.updateRecord(currentTrip);
            }
            if(pointrecord != null){
                pointrecord.setEndDate(new Date());
                pointlog.updateRecord((pointrecord));
            }
        }

        protected Dialog onCreateDialog(int id) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            switch (id) {
                case NO_BLUETOOTH_ID:
                    build.setMessage(getString(R.string.text_no_bluetooth_id));
                    return build.create();
                case BLUETOOTH_DISABLED:
                    build.setMessage(getString(R.string.text_bluetooth_disabled));
                    return build.create();
                case NO_ORIENTATION_SENSOR:
                    build.setMessage(getString(R.string.text_no_orientation_sensor));
                    return build.create();
                case NO_GPS_SUPPORT:
                    build.setMessage(getString(R.string.text_no_gps_support));
                    return build.create();
                case SAVE_TRIP_NOT_AVAILABLE:
                    build.setMessage(getString(R.string.text_save_trip_not_available));
                    return build.create();
            }
            return null;
        }

        public boolean onPrepareOptionsMenu(Menu menu) {
            MenuItem startItem = menu.findItem(START_LIVE_DATA);
            MenuItem stopItem = menu.findItem(STOP_LIVE_DATA);
            MenuItem settingsItem = menu.findItem(SETTINGS);
            MenuItem getDTCItem = menu.findItem(GET_DTC);

            if (service != null && service.isRunning()) {
                getDTCItem.setEnabled(false);
                startItem.setEnabled(false);
                stopItem.setEnabled(true);
                settingsItem.setEnabled(false);
            } else {
                getDTCItem.setEnabled(true);
                stopItem.setEnabled(false);
                startItem.setEnabled(true);
                settingsItem.setEnabled(true);
            }

            return true;
        }

        private void addTableRow(String id, String key, String val) {

            TableRow tr = new TableRow(this);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,
                    TABLE_ROW_MARGIN);
            tr.setLayoutParams(params);

            TextView name = new TextView(this);
            name.setGravity(Gravity.RIGHT);
            name.setText(key + ": ");
            TextView value = new TextView(this);
            value.setGravity(Gravity.LEFT);
            value.setText(val);
            value.setTag(id);
            tr.addView(name);
            tr.addView(value);
            tl.addView(tr, params);
        }

        /**
         *
         */
        private void queueCommands() {
            if (isServiceBound) {
                for (ObdCommand Command : ObdConfig.getCommands()) {
                    if (prefs.getBoolean(Command.getName(), true))
                        service.queueJob(new ObdCommandJob(Command));
                }
            }
        }


        private void doBindService() {
            if (!isServiceBound) {
                Log.d(TAG, "Binding OBD service..");
                if (preRequisites) {
                    btStatusTextView.setText(getString(R.string.status_bluetooth_connecting));
                    Intent serviceIntent = new Intent(this, ObdGatewayService.class);
                    bindService(serviceIntent, serviceConn, BIND_AUTO_CREATE);
                } else {
                    btStatusTextView.setText(getString(R.string.status_bluetooth_disabled));
                    Intent serviceIntent = new Intent(this, MockObdGatewayService.class);
                    bindService(serviceIntent, serviceConn, BIND_AUTO_CREATE);
                }
            }
        }

        private void doUnbindService() {
            if (isServiceBound) {
                if (service.isRunning()) {
                    service.stopService();
                    if (preRequisites)
                        btStatusTextView.setText(getString(R.string.status_bluetooth_ok));
                }
                Log.d(TAG, "Unbinding OBD service..");
                unbindService(serviceConn);
                isServiceBound = false;
                obdStatusTextView.setText(getString(R.string.status_obd_disconnected));
            }
        }

        public void onLocationChanged(Location location) {
            mLastLocation = location;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }

        public void onGpsStatusChanged(int event) {

            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    gpsStatusTextView.setText(getString(R.string.status_gps_started));
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    gpsStatusTextView.setText(getString(R.string.status_gps_stopped));
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    gpsStatusTextView.setText(getString(R.string.status_gps_fix));
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    break;
            }
        }

        private synchronized void gpsStart() {
            if (!mGpsIsStarted && mLocProvider != null && mLocService != null && mLocService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocService.requestLocationUpdates(mLocProvider.getName(), getGpsUpdatePeriod(prefs), getGpsDistanceUpdatePeriod(prefs), this);
                mGpsIsStarted = true;
            } else if (mGpsIsStarted && mLocProvider != null && mLocService != null) {
            } else {
                gpsStatusTextView.setText(getString(R.string.status_gps_no_support));
            }
        }

        private synchronized void gpsStop() {
            if (mGpsIsStarted) {
                mLocService.removeUpdates(this);
                mGpsIsStarted = false;
                gpsStatusTextView.setText(getString(R.string.status_gps_stopped));
            }
        }

        /**
         * Uploading asynchronous task
         */
        private class UploadAsyncTask extends AsyncTask<ObdReading, Void, Void> {

            @Override
            protected Void doInBackground(ObdReading... readings) {
                Log.d(TAG, "Uploading " + readings.length + " readings..");
                // instantiate reading service client
                final String endpoint = prefs.getString(UPLOAD_URL_KEY, "");
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint(endpoint)
                        .build();
                ObdService service = restAdapter.create(ObdService.class);
                // upload readings
                for (ObdReading reading : readings) {
                    try {
                        Response response = service.uploadReading(reading);
                        assert response.getStatus() == 200;
                    } catch (RetrofitError re) {
                        Log.e(TAG, re.toString());
                    }

                }
                Log.d(TAG, "Done");
                return null;
            }

        }
    }
}
