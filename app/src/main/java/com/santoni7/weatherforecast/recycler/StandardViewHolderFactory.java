package com.santoni7.weatherforecast.recycler;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.santoni7.weatherforecast.R;

public class StandardViewHolderFactory implements ViewHolderFactory{
    @Override
    public DailyViewHolder createViewHolder(ViewGroup parent) {
        View item_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new DailyViewHolderImpl(item_view);
    }
}
