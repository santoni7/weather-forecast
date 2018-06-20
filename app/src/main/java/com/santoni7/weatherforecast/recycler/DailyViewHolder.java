package com.santoni7.weatherforecast.recycler;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.santoni7.weatherforecast.model.ForecastByHour;

abstract class DailyViewHolder extends RecyclerView.ViewHolder{

    public DailyViewHolder(View itemView) {
        super(itemView);
    }

    abstract void setTypeface(Typeface typeface);
    abstract void setData(ForecastByHour fbh, Context context);
}
