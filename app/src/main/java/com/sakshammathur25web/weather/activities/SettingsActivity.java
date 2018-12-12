package com.sakshammathur25web.weather.activities;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.sakshammathur25web.weather.AlarmReceiver;
import com.sakshammathur25web.weather.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Thursday 2016-01-14 16:00:00
    private final Date SAMPLE_DATE = new Date(1452805200000L);

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkTheme",false)){
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        View bar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0);
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //noinspection deprecation
        addPreferencesFromResource(R.xml.prefs);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        setCustomDateEnabled();
        updateDateFormatList();

        // Set summaries to current value
        setListPreferenceSummary("unit");
        setListPreferenceSummary("lengthUnit");
        setListPreferenceSummary("speedUnit");
        setListPreferenceSummary("pressureUnit");
        setListPreferenceSummary("refreshInterval");
        setListPreferenceSummary("windDirectionFormat");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "unit":
            case "lengthUnit":
            case "speedUnit":
            case "pressureUnit":
            case "windDirectionFormat":
                setListPreferenceSummary(key);
                break;
            case "refreshInterval":
                setListPreferenceSummary(key);
                AlarmReceiver.setRecurringAlarm(this);
                break;
            case "dateFormat":
                setCustomDateEnabled();
                setListPreferenceSummary(key);
                break;
            case "dateFormatCustom":
                updateDateFormatList();
                break;
            case "theme":
                // Restart activity to apply theme
                overridePendingTransition(0, 0);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                break;
                default:
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION){
            boolean permissionGranted = grantResults.length>0&&grantResults[0]==PackageManager
                    .PERMISSION_GRANTED;
            CheckBoxPreference checkBox = (CheckBoxPreference)findPreference("updateLocationAutomatically");
            checkBox.setChecked(permissionGranted);
            if (permissionGranted){
                privacyGuardWorkaround();
            }
        }
    }

    private void privacyGuardWorkaround(){
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            DummyLocationListener dummyLocationListener = new DummyLocationListener();
            assert locationManager != null;
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,
                    0,dummyLocationListener);
            locationManager.removeUpdates(dummyLocationListener);
        }catch (SecurityException e){
            //This will probably not happen, as we just granted the permission
        }
    }

    private void setListPreferenceSummary(String preferenceKey) {
        ListPreference preference = (ListPreference) findPreference(preferenceKey);//unused
    }

    private void setCustomDateEnabled() {
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        Preference customDatePref = findPreference("dateFormatCustom");
        customDatePref.setEnabled("custom".equals(sp.getString("dateFormat", "")));
    }

    @SuppressLint("SimpleDateFormat")
    private void updateDateFormatList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();

        ListPreference dateFormatPref = (ListPreference) findPreference("dateFormat");
        String[] dateFormatsValues = res.getStringArray(R.array.dateFormatValues);
        String[] dateFormatsEntries = new String[dateFormatsValues.length];

        EditTextPreference customDateFormatPref = (EditTextPreference) findPreference("dateFormatCustom");
        customDateFormatPref.setDefaultValue(dateFormatsValues[0]);

        SimpleDateFormat sdformat = new SimpleDateFormat();
        for (int i=0; i<dateFormatsValues.length; i++) {
            String value = dateFormatsValues[i];
            if ("custom".equals(value)) {
                String renderedCustom;
                try {
                    sdformat.applyPattern(sp.getString("dateFormatCustom", dateFormatsValues[0]));
                    renderedCustom = sdformat.format(SAMPLE_DATE);
                } catch (IllegalArgumentException e) {
                    renderedCustom = res.getString(R.string.error_dateFormat);
                }
                dateFormatsEntries[i] = String.format("%s:\n%s",
                        res.getString(R.string.setting_dateFormatCustom),
                        renderedCustom);
            } else {
                sdformat.applyPattern(value);
                dateFormatsEntries[i] = sdformat.format(SAMPLE_DATE);
            }
        }

        dateFormatPref.setDefaultValue(dateFormatsValues[0]);
        dateFormatPref.setEntries(dateFormatsEntries);

        setListPreferenceSummary("dateFormat");
    }

    class DummyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}