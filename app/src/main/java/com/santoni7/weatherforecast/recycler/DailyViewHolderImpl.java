package com.santoni7.weatherforecast.recycler;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.santoni7.weatherforecast.R;
import com.santoni7.weatherforecast.model.Forecast;
import com.santoni7.weatherforecast.model.ForecastByHour;
import com.santoni7.weatherforecast.util.TextHelpers;


public class DailyViewHolderImpl extends DailyViewHolder {
    private TextView txtDayName;
    private TextView txtDescription;
    private TextView txtIcon;
    private TextView txtMinTemp;
    private TextView txtMaxTemp;

    public DailyViewHolderImpl(View v) {
        super(v);
        txtDayName = v.findViewById(R.id.il_txtDayName);
        txtDescription = v.findViewById(R.id.il_txtDescription);
        txtIcon = v.findViewById(R.id.il_txtIcon);
        txtMaxTemp = v.findViewById(R.id.il_txtTempMax);
        txtMinTemp = v.findViewById(R.id.il_txtTempMin);
    }

    @Override
    public void setTypeface(Typeface typeface) {
        getTxtIcon().setTypeface(typeface);
    }

    @Override
    public void setData(ForecastByHour fbh, Context context) {
        Forecast midday = fbh.getMiddayForecast();
        getTxtDayName().setText(TextHelpers.GetShortDayString(fbh.getMiddayForecast()));
        String icName = TextHelpers.GetIconName(midday);
        getTxtIcon().setText(icName);
        if (TextHelpers.IsIconYellow(icName))
            getTxtIcon().setTextColor(ContextCompat.getColor(context, R.color.colorYellow));
        else getTxtIcon().setTextColor(ContextCompat.getColor(context, R.color.colorIcon));
        getTxtDescription().setText(midday.getWeatherDescription());
        getTxtMinTemp().setText(TextHelpers.GetTemperatureString(fbh.getMinTemp()));
        getTxtMaxTemp().setText(TextHelpers.GetTemperatureString(fbh.getMaxTemp()));
    }

    public TextView getTxtDayName() {
        return txtDayName;
    }

    public TextView getTxtDescription() {
        return txtDescription;
    }


    public TextView getTxtIcon() {
        return txtIcon;
    }


    public TextView getTxtMinTemp() {
        return txtMinTemp;
    }


    public TextView getTxtMaxTemp() {
        return txtMaxTemp;
    }

}
