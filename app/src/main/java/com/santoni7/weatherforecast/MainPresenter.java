package com.santoni7.weatherforecast;

import android.util.Log;

import com.santoni7.weatherforecast.model.GeoData;
import com.santoni7.weatherforecast.mvp.PresenterBase;
import com.santoni7.weatherforecast.util.JsonParser;
import com.santoni7.weatherforecast.util.OfflineWeatherStorage;
import com.santoni7.weatherforecast.util.PreferenceManager;

import java.util.Calendar;


public class MainPresenter extends PresenterBase<MainContract.View> implements MainContract.Presenter {
    private static final String TAG = MainPresenter.class.getSimpleName();
    Calendar lastUpdate;
    GeoData geoData = new GeoData(false, "London", "London");


    @Override
    public void onCreated() {
        PreferenceManager preferenceManager = PreferenceManager.getInstance();

        String location = preferenceManager.getLocationName();
        if (location != null) {
            geoData.locationName = preferenceManager.getLocationName();
            geoData.locationNameLocalized = preferenceManager.getLocationNameLocalized();
            geoData.lastUpdate = Calendar.getInstance();
            getView().requestWeatherUpdate(geoData);
        }

        getView().requestGeoUpdate();
    }

    @Override
    public void onGeoResult(GeoData geoData) {
        this.geoData = geoData;
        PreferenceManager pm = PreferenceManager.getInstance();
        if(pm != null && geoData.success){
            pm.setLastUpdated(geoData.lastUpdate.getTimeInMillis());
            pm.setLocationName(geoData.locationName);
            pm.setLocationNameLocalized(geoData.locationNameLocalized);
        }
        Log.d(TAG, "OnGeoResult: " + geoData.locationName);
        if (getView() != null) {
            getView().requestWeatherUpdate(geoData);
        }
    }

    @Override
    public void onWeatherResult(String res) {
        Log.i(TAG, "onWeatherResult: " + res);
        JsonParser jp = new JsonParser(res);
        lastUpdate = Calendar.getInstance();

        PreferenceManager.getInstance().setLastUpdated(lastUpdate.getTimeInMillis());
        getView().updateView(jp);
    }

    @Override
    public void onResume() {
        getView().requestGeoUpdate();
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onGpsNotAvailable() {
        if (getView() != null)
            getView().alertDialogNoGPS();
    }


    @Override
    public void onBtnClick(int id) {
        switch (id) {
            case R.id.btnAbout:
                getView().aboutDialog();
                break;
            case R.id.btnRefresh:
                getView().requestWeatherUpdate(geoData);
                getView().requestGeoUpdate();
                break;
        }
    }
}
