package com.santoni7.weatherforecast.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

//Todo: GeoService not available on android 4.4
public class GeoService extends IntentService {
    private final String TAG = "GeoService";
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private LocationRequest mLocationRequest;
    public GeoService() {
        super("geoservice");
    }

    public final static String ACTION_LOCATION_RESULT = "com.santoni7.weatherforecast.GeoService.LOCATION_RESULT";
    public final static String EXTRA_RESULTOK_OUT = "com.santoni7.weatherforecast.GeoService.EXTRA_RESULTOK_OUT";
    public final static String EXTRA_LOCATION_OUT = "com.santoni7.weatherforecast.GeoService.EXTRA_LOCATION_OUT";
    public final static String EXTRA_CITY_OUT = "com.santoni7.weatherforecast.GeoService.EXTRA_CITY_OUT";
    public final static String EXTRA_CITY_LOCALIZED_OUT = "com.santoni7.weatherforecast.GeoService.EXTRA_CITY_LOCALIZED_OUT";

    public final static String EXTRA_FORCE_UPDATE = "com.santoni7.weatherforecast.GeoService.EXTRA_FORCE_UPDATE";
    boolean forceUpdate = false;
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        forceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);
        initApi(0);
    }

    private void initApi(int delay) {
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GoogleApiCallbacks callbacks = new GoogleApiCallbacks();
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(callbacks)
                            .addOnConnectionFailedListener(callbacks)
                            .build();
                    mGoogleApiClient.connect();
                    mLocationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                            .setInterval(10 * 1000)      //10sec
                            .setFastestInterval(1000);
                }
                else{
                    mGoogleApiClient.reconnect();
                }
            }
        }, delay);

    }

    @Override
    public void onDestroy() {
        //mGoogleApiClient.disconnect();

        super.onDestroy();
    }

    private class GoogleApiCallbacks implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.i(TAG, "GoogleApiCallbacks/onConnected");
            try {
                mLastLocation =LocationServices.FusedLocationApi.getLastLocation (mGoogleApiClient);

                if (mLastLocation != null) {
                    sendResult();
                }
                else{
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    sendErrorResponse();
                }
            }
            catch (SecurityException | IOException se){
                se.fillInStackTrace();
                Log.e(TAG, se.getMessage());
                sendErrorResponse();
            }
        }

        private void sendResult() throws IOException {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Intent resultIntent = new Intent();
            resultIntent.setAction(ACTION_LOCATION_RESULT)
                    .addCategory(Intent.CATEGORY_DEFAULT);
            String location_str = "lat=" + mLastLocation.getLatitude() + "&lon=" + mLastLocation.getLongitude();
            resultIntent.putExtra(EXTRA_LOCATION_OUT, location_str);
            Geocoder geocoder = new Geocoder(GeoService.this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            String cityStr = list.get(0).getLocality();
            if(list.size()>0){
                resultIntent.putExtra(EXTRA_CITY_OUT, cityStr);
                Address address = list.get(0);

                resultIntent.putExtra(EXTRA_CITY_LOCALIZED_OUT, list.get(0).getLocality());
            }
            resultIntent.putExtra(EXTRA_RESULTOK_OUT, true);


            Log.i(TAG, "Sending result: " + location_str + "; City: " + cityStr);
            sendBroadcast(resultIntent);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(TAG, "GoogleApiCallbacks/onConnectionSuspended");
            sendErrorResponse();
        }

        private void sendErrorResponse() {
            Log.i(TAG, "GoogleApiCallbacks/sendErrorResponse");
            if(forceUpdate)
                initApi(4000);
            Intent resultIntent = new Intent().setAction(ACTION_LOCATION_RESULT)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .putExtra(EXTRA_RESULTOK_OUT, false);
            sendBroadcast(resultIntent);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "GoogleApiCallbacks/onLocationChanged");
            mLastLocation = location;
            try {
                sendResult();
            }
            catch (IOException e){
                e.fillInStackTrace();
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.i(TAG, "GoogleApiCallbacks/onConnectionFailed");
            sendErrorResponse();
        }
    }
}
