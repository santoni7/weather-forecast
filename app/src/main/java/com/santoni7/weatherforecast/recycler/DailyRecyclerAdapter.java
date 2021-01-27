package com.santoni7.weatherforecast.recycler;


import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.santoni7.weatherforecast.model.DailyForecastMap;
import com.santoni7.weatherforecast.model.ForecastByHour;

import java.util.List;

public class DailyRecyclerAdapter extends RecyclerView.Adapter<DailyViewHolder> {

    private List<ForecastByHour> list;
    private Typeface iconTypeface;
    private Context context;

    // Abstract factory instance
    private ViewHolderFactory factory = new StandardViewHolderFactory();
    public DailyRecyclerAdapter(DailyForecastMap dailyForecastMap, Typeface iconTypeface) {
        this.iconTypeface = iconTypeface;
        list = dailyForecastMap.asList();
        list.remove(0);//remove today's entry
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DailyViewHolder viewHolder = factory.createViewHolder(parent);
        this.context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        if(list == null) return;
        ForecastByHour fbh = list.get(position);
        holder.setData(fbh, context);
        holder.setTypeface(iconTypeface);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
