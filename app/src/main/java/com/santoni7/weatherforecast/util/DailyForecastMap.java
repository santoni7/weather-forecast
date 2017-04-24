package com.santoni7.weatherforecast.util;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class DailyForecastMap extends TreeMap<Integer, ForecastByHour> {
    public List<ForecastByHour> asList(){
        List<ForecastByHour> res = new ArrayList<>();
        for(int k : keySet()){
            res.add(get(k));

        }
        return res;
    }
}
