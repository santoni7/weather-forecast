package com.santoni7.weatherforecast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.santoni7.weatherforecast.service.GeoService;
import com.santoni7.weatherforecast.service.WeatherPullService;
import com.santoni7.weatherforecast.util.JsonResponseWrapper;
import com.santoni7.weatherforecast.util.OfflineWeatherStorage;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    WeatherBroadcastReceiver weatherBroadcastReceiver;
    GeoServiceReceiver geoServiceReceiver;
    String currentCity = "London";
    String currentCityLocalized;
    Calendar lastUpdate;

    Intent weatherPullIntent;
    JsonResponseWrapper wrapper;

    SharedPreferences sPref;
    static final String PREF_LOCATION_KEY = "LocationCity";
    static final String PREF_LOCATION_LOCALIZED_KEY = "LocalizedCity";
    static final String PREF_LAST_UPDATE = "LastUpdate";

    ProgressDialog loadingDialog;
    long ldStartedMs;
    static final long GEO_TIMEOUT = 3000;

    LinearLayout fragmentContainer;

    MainFragment mainFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherBroadcastReceiver = new WeatherBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(WeatherPullService.ACTION_RESULT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(weatherBroadcastReceiver, intentFilter);


        requestGeoUpdate();
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M ) {
            checkPermission();
        }

        geoServiceReceiver = new GeoServiceReceiver();
        intentFilter = new IntentFilter(GeoService.ACTION_LOCATION_RESULT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(geoServiceReceiver, intentFilter);

        sPref = getPreferences(MODE_PRIVATE);
        if(sPref.contains(PREF_LOCATION_KEY)){
            currentCity = sPref.getString(PREF_LOCATION_KEY, null);
            currentCityLocalized = sPref.getString(PREF_LOCATION_LOCALIZED_KEY, null);
            lastUpdate = Calendar.getInstance();
            lastUpdate.setTimeInMillis(sPref.getLong(PREF_LAST_UPDATE, 0));
            requestWeatherUpdate();
        }

        String weather = OfflineWeatherStorage.Read(this);
        if(weather!=null){
            wrapper = new JsonResponseWrapper(weather);
        }


        fragmentContainer = (LinearLayout) findViewById(R.id.fragment_container);

        mainFragment = new MainFragment();
        if(wrapper != null) mainFragment.setWrapper(wrapper, currentCityLocalized, lastUpdate);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, mainFragment);
        ft.commit();


        if(wrapper == null || wrapper.getForecastByDay().isEmpty()){
            buildProgressDialog();
        }
    }

    private void buildProgressDialog() {
        if(currentCity == null) {
            checkPermission();
            return;
        }
        loadingDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        loadingDialog.setCancelable(true);
        loadingDialog.setMessage(getString(R.string.string_loading_message));
        loadingDialog.show();
        ldStartedMs = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(currentCity == null) {
            requestGeoUpdate();
        }
    }

    public void onBtnClick(View v){
        switch (v.getId()){
            case R.id.btnAbout:
                buildAboutDialog();
                break;
            case R.id.btnRefresh:
                requestWeatherUpdate();
                break;
        }
    }

    @Override
    protected void onPostResume() {
        if(mainFragment!=null && wrapper!=null){
            mainFragment.setWrapper(wrapper, currentCityLocalized, lastUpdate);
            mainFragment.updateData();
        }
        super.onPostResume();
    }

    private void alertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.request_gps)
                .setCancelable(false)
                .setPositiveButton(R.string.string_yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.string_no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAboutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setCancelable(true)
                .setPositiveButton("OK", null);
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(weatherBroadcastReceiver);
            unregisterReceiver(geoServiceReceiver);
        }catch (Exception r){
            r.fillInStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(fragmentContainer, "This app needs to be allowed to use GPS", Snackbar.LENGTH_SHORT).show();
            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        123);
            }
        }
    }

    public void requestGeoUpdate(){
        checkPermission();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean forceUpdate = currentCity == null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertMessageNoGps();
            forceUpdate = true;
        }

        Intent mGeoIntent = new Intent(this, GeoService.class);
        mGeoIntent.putExtra(GeoService.EXTRA_FORCE_UPDATE, forceUpdate);
        startService(mGeoIntent);
    }

    public void requestWeatherUpdate() {
        if(currentCity == null || currentCity.isEmpty()) {
            Toast.makeText(this, "Could not request weather update: unknown location", Toast.LENGTH_SHORT).show();
            return;
        }
        weatherPullIntent = new Intent(this, WeatherPullService.class);
        weatherPullIntent.putExtra(WeatherPullService.EXTRA_LOCATIONTYPE, String.valueOf(WeatherPullService.LocationType.BY_CITY))
                .putExtra(WeatherPullService.EXTRA_LOCATION, currentCity)
                .putExtra(WeatherPullService.EXTRA_APPID, getString(R.string.openweather_appid));
        startService(weatherPullIntent);
    }

    public class WeatherBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean error = intent.getBooleanExtra(WeatherPullService.EXTRA_RESULT_IS_ERROR, false);
                if(error){
                    Snackbar.make(fragmentContainer, "Could not pull forecast", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (loadingDialog != null) {
                    loadingDialog.cancel();
                }
                String result = intent
                        .getStringExtra(WeatherPullService.EXTRA_RESULT_JSON_OUT);
                if (result == null || result.equals("error")) {
                    Toast.makeText(context, "Could not pull forecast", Toast.LENGTH_SHORT).show();
                }
                wrapper = new JsonResponseWrapper(result);
                OfflineWeatherStorage.Write(getApplicationContext(), result);
                lastUpdate = Calendar.getInstance();
                sPref.edit().putLong(PREF_LAST_UPDATE, lastUpdate.getTimeInMillis()).apply();
                mainFragment.setWrapper(wrapper, currentCityLocalized, lastUpdate);
                mainFragment.updateData();
            }
            catch (Exception e){
                e.fillInStackTrace();
            }
        }
    }

    public class GeoServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(TAG, "GeoServiceReceiver/onReceive");
                boolean success = intent.getBooleanExtra(GeoService.EXTRA_RESULTOK_OUT, false);
                if (success) {
                    currentCity = intent.getStringExtra(GeoService.EXTRA_CITY_OUT);
                    currentCityLocalized = intent.getStringExtra(GeoService.EXTRA_CITY_LOCALIZED_OUT);
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putString(PREF_LOCATION_KEY, currentCity);
                    editor.putString(PREF_LOCATION_LOCALIZED_KEY, currentCityLocalized);
                    editor.apply();
                    requestWeatherUpdate();
                } else {
                    if (loadingDialog != null && loadingDialog.isShowing() && System.currentTimeMillis() - ldStartedMs > GEO_TIMEOUT) {
                        loadingDialog.cancel();
                        alertMessageNoGps();
                        ldStartedMs = System.currentTimeMillis();
                    }
                }
            }catch(Exception e){
                e.fillInStackTrace();
            }
        }
    }

}
