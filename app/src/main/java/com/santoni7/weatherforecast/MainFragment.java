package com.santoni7.weatherforecast;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.santoni7.weatherforecast.model.Forecast;
import com.santoni7.weatherforecast.model.GeoData;
import com.santoni7.weatherforecast.util.DailyForecastAdapter;
import com.santoni7.weatherforecast.model.ForecastByHour;
import com.santoni7.weatherforecast.util.Helpers;
import com.santoni7.weatherforecast.util.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getSimpleName();
    TextView txtLargeIcon;
    TextView txtLargeTemp;
    TextView txtCity;
    TextView txtDescription;
    TextView txtDayDate;

    TextView txtTempMorning;
    TextView txtTempDay;
    TextView txtTempEvening;
    TextView txtTempNight;

    TextView txtHumidity;
    TextView txtWind;
    TextView icHumidity;
    TextView icWind;
    TextView txtMaxTemp;
    TextView txtMinTemp;
    TextView icMaxTemp;
    TextView icMinTemp;

    TextView ic_morning;
    TextView ic_day;
    TextView ic_evening;
    TextView ic_night;

    TextView txtLastUpdate;

    Typeface typefaceIcons;


    RecyclerView recyclerView;
    JsonParser jsonParser;
    private GeoData geoData;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        typefaceIcons = Typeface.createFromAsset(inflater.getContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    void setDefaultIcon(TextView target) {
        target.setTypeface(typefaceIcons);
        target.setText(R.string.i_sunny);
    }

    void setIcon(TextView target, int strId) {
        target.setTypeface(typefaceIcons);
        target.setText(strId);
    }

    void setIcon(TextView target, String str) {
        target.setTypeface(typefaceIcons);
        target.setText(str);
        if (Helpers.IsIconYellow(str)) {
            target.setTextColor(ContextCompat.getColor(getContext(), R.color.colorYellow));
        } else {
            target.setTextColor(ContextCompat.getColor(getContext(), R.color.colorIcon));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtLargeIcon = (TextView) view.findViewById(R.id.txtLargeIcon);
        setDefaultIcon(txtLargeIcon);
        txtLargeTemp = (TextView) view.findViewById(R.id.txtLargeTemp);
        txtDescription = (TextView) view.findViewById(R.id.txtDescription);
        txtCity = (TextView) view.findViewById(R.id.txtCityName);
        txtDayDate = (TextView) view.findViewById(R.id.txtDayDate);

        ic_morning = (TextView) view.findViewById(R.id.ic_morning);
        ic_day = (TextView) view.findViewById(R.id.ic_day);
        ic_evening = (TextView) view.findViewById(R.id.ic_evening);
        ic_night = (TextView) view.findViewById(R.id.ic_night);
        setDefaultIcon(ic_morning);
        setDefaultIcon(ic_day);
        setDefaultIcon(ic_evening);
        setDefaultIcon(ic_night);

        txtTempMorning = (TextView) view.findViewById(R.id.txt_temp_morning);
        txtTempDay = (TextView) view.findViewById(R.id.txt_temp_day);
        txtTempEvening = (TextView) view.findViewById(R.id.txt_temp_evening);
        txtTempNight = (TextView) view.findViewById(R.id.txt_temp_night);

        txtHumidity = (TextView) view.findViewById(R.id.txtHumidity);
        txtWind = (TextView) view.findViewById(R.id.txtWind);
        icHumidity = (TextView) view.findViewById(R.id.ic_humidity_lbl);
        icWind = (TextView) view.findViewById(R.id.ic_wind_lbl);
        setIcon(icHumidity, R.string.i_humidity);
        setIcon(icWind, R.string.i_wind);

        txtMaxTemp = (TextView) view.findViewById(R.id.txtMaxTemp);
        txtMinTemp = (TextView) view.findViewById(R.id.txtMinTemp);
        icMaxTemp = (TextView) view.findViewById(R.id.ic_max_temp);
        icMinTemp = (TextView) view.findViewById(R.id.ic_min_temp);
        setIcon(icMaxTemp, R.string.i_max_temp);
        setIcon(icMinTemp, R.string.i_min_temp);

        txtLargeTemp.setText("18" + getString(R.string.celcius));

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(lm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                lm.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        txtLastUpdate = (TextView) view.findViewById(R.id.txtLastUpdate);

    }


    @Override
    public void onResume() {

        if (jsonParser != null) updateData();
        super.onResume();
    }

    public void updateData(JsonParser wrapper, GeoData geo) {
        setData(wrapper, geo);
        updateData();
        
    }

    public void updateData() {
        if (jsonParser == null ||
                txtLargeTemp == null ||
                jsonParser.getForecastByDay().isEmpty()) {
            Log.e(TAG, "updateData() failed");
            return;
        }
        ForecastByHour fbh = jsonParser.getForecastByDay().firstEntry().getValue();
        Forecast current = fbh.firstEntry().getValue();
        if (current.getWeatherId() == 800) {
            txtLargeIcon.setTextColor(ContextCompat.getColor(getContext(), R.color.colorYellow));
        } else {
            txtLargeIcon.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGrey));
        }
        setIcon(txtLargeIcon, Helpers.GetIconName(current));
        txtLargeTemp.setText(Helpers.GetTemperatureString(current.getTemperature()));
        if (geoData != null)
            txtCity.setText(geoData.locationNameLocalized);
        txtDescription.setText(Helpers.FirstToUpperCase(current.getWeatherDescription()));
        txtDayDate.setText(Helpers.GetDayDateString(current));

        setTodayTemperatures(fbh);

        txtHumidity.setText(current.getHumidity() + "");
        String windStr = Math.round(current.getWind_speed() * 100) / 100.0 + "m/s";
        txtWind.setText(windStr);

        DailyForecastAdapter adapter = new DailyForecastAdapter(jsonParser.getForecastByDay(), typefaceIcons);
        recyclerView.setAdapter(adapter);

        if (geoData != null && geoData.lastUpdate != null) {
            String lastUpdateString = "Последнее обновление: ";
            SimpleDateFormat format1 = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
            lastUpdateString += format1.format(geoData.lastUpdate.getTime());
            txtLastUpdate.setText(lastUpdateString);
        }
    }

    private void setTodayTemperatures(ForecastByHour fbh) {
        //morning
        if (fbh.size() >= 7) {
            Forecast f = fbh.get(6);
            txtTempMorning.setText(Helpers.GetTemperatureString(f.getTemperature()));
            setIcon(ic_morning, Helpers.GetIconName(f));
        } else {
            txtTempMorning.setText("-");
            ic_morning.setVisibility(View.INVISIBLE);
        }
        //day
        if (fbh.size() >= 5) {
            Forecast f = fbh.get(12);
            txtTempDay.setText(Helpers.GetTemperatureString(f.getTemperature()));
            setIcon(ic_day, Helpers.GetIconName(f));
        } else if (fbh.size() >= 4) {
            Forecast f = fbh.get(15);
            txtTempDay.setText(Helpers.GetTemperatureString(f.getTemperature()));
            setIcon(ic_day, Helpers.GetIconName(f));
        } else {
            txtTempDay.setText("-");
            ic_day.setVisibility(View.INVISIBLE);
        }
        //evening
        if (fbh.size() >= 3) {
            Forecast f = fbh.get(18);
            txtTempEvening.setText(Helpers.GetTemperatureString(f.getTemperature()));
            setIcon(ic_evening, Helpers.GetIconName(f));
        } else if (fbh.size() >= 2) {
            Forecast f = fbh.get(21);
            txtTempEvening.setText(Helpers.GetTemperatureString(f.getTemperature()));
            setIcon(ic_evening, Helpers.GetIconName(f));
        } else {
            txtTempEvening.setText("-");
            ic_evening.setVisibility(View.INVISIBLE);
        }

        double minTemp = fbh.getMinTemp();
        if (jsonParser.getForecastByDay().size() > 1) {
            Forecast f = jsonParser.getForecastByDay().get(1).firstEntry().getValue();
            double temperature = f.getTemperature();
            txtTempNight.setText(Helpers.GetTemperatureString(temperature));
            setIcon(ic_night, Helpers.GetIconName(f));
            if (temperature < minTemp) minTemp = temperature;
        } else {
            txtTempNight.setText("-");
            ic_night.setVisibility(View.INVISIBLE);
        }

        txtMaxTemp.setText(Helpers.GetTemperatureString(fbh.getMaxTemp()));
        txtMinTemp.setText(Helpers.GetTemperatureString(minTemp));


    }

    public void setData(JsonParser jsonParser, GeoData geo) {
        this.jsonParser = jsonParser;
        this.geoData = geo;
    }
}
