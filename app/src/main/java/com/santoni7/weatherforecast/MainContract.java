package com.santoni7.weatherforecast;

import android.content.Context;
import android.content.SharedPreferences;

import com.santoni7.weatherforecast.model.GeoData;
import com.santoni7.weatherforecast.mvp.MvpPresenter;
import com.santoni7.weatherforecast.mvp.MvpView;
import com.santoni7.weatherforecast.util.JsonParser;

public class MainContract {
    private MainContract(){}

    interface View extends MvpView{
        void alertDialogNoGPS();
        void aboutDialog();
        void requestGeoUpdate();
        void requestWeatherUpdate(GeoData geo);
        void updateView(JsonParser jsonParser);
        Context getContext();
        SharedPreferences getDefaultPreferences();
    }

    interface Presenter extends MvpPresenter<View>{
        void onCreated();
        void onResume();
        void onStop();

        void onGpsNotAvailable();
        void onGeoResult(GeoData geoData);
        void onBtnClick(int id);
        void onWeatherResult(String res);

    }
}
