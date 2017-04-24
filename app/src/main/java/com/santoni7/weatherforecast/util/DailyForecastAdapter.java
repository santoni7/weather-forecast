package com.santoni7.weatherforecast.util;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.santoni7.weatherforecast.R;

import java.util.List;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {

    DailyForecastMap dailyForecastMap;
    List<ForecastByHour> list;
    Typeface iconTypeface;
    public DailyForecastAdapter(DailyForecastMap dailyForecastMap, Typeface iconTypeface) {
        this.dailyForecastMap = dailyForecastMap;
        this.iconTypeface = iconTypeface;
        list = dailyForecastMap.asList();
        list.remove(0);//remove today
    }

    Context context;
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder vh = new ViewHolder(item_view);
        this.context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(list == null) return;
        ForecastByHour fbh = list.get(position);
        JsonResponseWrapper.Forecast midday = fbh.getMiddayForecast();
        holder.txtDayName.setText(Helpers.GetShortDayString(fbh.getMiddayForecast()));
        holder.txtIcon.setTypeface(iconTypeface);
        String icName = Helpers.GetIconName(midday);
        holder.txtIcon.setText(icName);
        if(Helpers.IsIconYellow(icName)) holder.txtIcon.setTextColor(ContextCompat.getColor(context, R.color.colorYellow));
        else holder.txtIcon.setTextColor(ContextCompat.getColor(context, R.color.colorIcon));
        holder.txtDescription.setText(midday.getWeatherDescription());
        holder.txtMinTemp.setText(Helpers.GetTemperatureString(fbh.getMinTemp()));
        holder.txtMaxTemp.setText(Helpers.GetTemperatureString(fbh.getMaxTemp()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        TextView txtDayName;
        TextView txtDescription;
        TextView txtIcon;
        TextView txtMinTemp;
        TextView txtMaxTemp;
        public ViewHolder(View v) {
            super(v);
            txtDayName = (TextView) v.findViewById(R.id.il_txtDayName);
            txtDescription = (TextView) v.findViewById(R.id.il_txtDescription);
            txtIcon = (TextView) v.findViewById(R.id.il_txtIcon);
            txtMaxTemp = (TextView) v.findViewById(R.id.il_txtTempMax);
            txtMinTemp = (TextView) v.findViewById(R.id.il_txtTempMin);

        }
    }
}
