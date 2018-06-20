package com.santoni7.weatherforecast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.santoni7.weatherforecast.model.GeoData;
import com.santoni7.weatherforecast.service.GeoService;
import com.santoni7.weatherforecast.service.WeatherPullService;
import com.santoni7.weatherforecast.util.JsonParser;
import com.santoni7.weatherforecast.util.OfflineWeatherStorage;
import com.santoni7.weatherforecast.util.PreferenceManager;

public class MainActivity extends AppCompatActivity implements MainContract.View {
    private final static String TAG = MainActivity.class.getSimpleName();

    MainContract.Presenter presenter;
    WeatherBroadcastReceiver weatherBroadcastReceiver;
    GeoServiceReceiver geoServiceReceiver;
    //    String locationName = "London";
//    String locationNameLocalized;
//    Calendar lastUpdate;

    Intent weatherPullIntent;
//    JsonParser jsonParser;
    GeoData geoData = new GeoData(false, null, null);

    ProgressDialog loadingDialog;
    long ldStartedMs;
    static final long GEO_TIMEOUT = 3000;

    LinearLayout fragmentContainer;

    MainFragment mainFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(TAG, "Error occurred: " + e.getMessage());
                e.fillInStackTrace();
            }
        });

        setContentView(R.layout.activity_main);
        PreferenceManager.getInstance(this);

        presenter = new MainPresenter();
        presenter.attachView(this);


        weatherBroadcastReceiver = new WeatherBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(WeatherPullService.ACTION_RESULT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(weatherBroadcastReceiver, intentFilter);


        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            checkPermission();
        }

        geoServiceReceiver = new GeoServiceReceiver();
        intentFilter = new IntentFilter(GeoService.ACTION_LOCATION_RESULT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(geoServiceReceiver, intentFilter);


        String weatherJson = OfflineWeatherStorage.Read(this);

        mainFragment = new MainFragment();
        JsonParser jsonParser = null;
        if (weatherJson != null) {
            jsonParser = new JsonParser(weatherJson);
            mainFragment.setData(jsonParser, geoData);

        }

        if (jsonParser == null || jsonParser.getForecastByDay().isEmpty()) {
            buildProgressDialog();
        }


        fragmentContainer = (LinearLayout) findViewById(R.id.fragment_container);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, mainFragment);
        ft.commit();

        presenter.onCreated();
    }

    @Override
    public void updateView(JsonParser jsonParser) {
        mainFragment.updateView(jsonParser, geoData);
    }

    void buildProgressDialog() {
        if (!geoData.success) {
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
        presenter.onResume();
    }

    public void onBtnClick(View v) {
        presenter.onBtnClick(v.getId());
    }


    @Override
    public void alertDialogNoGPS() {
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

    @Override
    public void aboutDialog() {
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
            presenter.onStop();
            unregisterReceiver(weatherBroadcastReceiver);
            unregisterReceiver(geoServiceReceiver);
        } catch (Exception r) {
            r.fillInStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {//Can add more as per requirement

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(fragmentContainer, "This app needs to be allowed to use GPS", Snackbar.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        123);
            }
        }
    }

    @Override
    public void requestGeoUpdate() {
        checkPermission();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean forceUpdate = geoData.locationName == null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertDialogNoGPS();
            forceUpdate = true;
        }

        Intent mGeoIntent = new Intent(this, GeoService.class);
        mGeoIntent.putExtra(GeoService.EXTRA_FORCE_UPDATE, forceUpdate);
        startService(mGeoIntent);
    }

    @Override
    public void requestWeatherUpdate(GeoData data) {
        weatherPullIntent = new Intent(this, WeatherPullService.class);
        weatherPullIntent.putExtra(WeatherPullService.EXTRA_LOCATIONTYPE, String.valueOf(WeatherPullService.LocationType.BY_CITY))
                .putExtra(WeatherPullService.EXTRA_LOCATION, data.locationName)
                .putExtra(WeatherPullService.EXTRA_APPID, getString(R.string.openweather_appid));
        startService(weatherPullIntent);
    }

    public class WeatherBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean error = intent.getBooleanExtra(WeatherPullService.EXTRA_RESULT_IS_ERROR, false);
                if (error) {
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
                OfflineWeatherStorage.Write(MainActivity.this, result);

                presenter.onWeatherResult(result);



            } catch (Exception e) {
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
                    geoData = new GeoData(intent);
                    PreferenceManager pm = PreferenceManager.getInstance();
                    pm.setLocationName(geoData.locationName);
                    pm.setLocationNameLocalized(geoData.locationNameLocalized);
                    presenter.onGeoResult(geoData);
                } else {
                    if (loadingDialog != null && loadingDialog.isShowing() && System.currentTimeMillis() - ldStartedMs > GEO_TIMEOUT) {
                        loadingDialog.cancel();
                        alertDialogNoGPS();
                        ldStartedMs = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
    }

}
