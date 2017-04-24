package com.santoni7.weatherforecast.util;


import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class JsonResponseWrapper {


    private DailyForecastMap forecastByDay;
    private JSONObject json;

    private String jsonString;
    public String getJsonString() {
        return jsonString;
    }

    private boolean valid = false;

    private static Calendar now = Calendar.getInstance();
    public City city;

    public JsonResponseWrapper(String jsonString) {
        try {
            this.jsonString = jsonString;
            json = new JSONObject(jsonString);
            city = new City(json.getJSONObject("city"));
            int cnt = json.getInt("cnt");
            JSONArray arr = json.getJSONArray("list");

            forecastByDay = new DailyForecastMap();
            ForecastByHour lastFBH = null;
            int minDay = 366;
            for(int i = 0; i < cnt; ++i){
                JSONObject obj = arr.getJSONObject(i);
                Forecast forecast = new Forecast();
                forecast.dateUTC = obj.getLong("dt");
                forecast.dateString = obj.getString("dt_txt");
                //forecast.setDate(new Date(forecast.dateString));
                forecast.calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    forecast.calendar.setTime(sdf.parse(forecast.getDateString()));
                }catch (ParseException pe){
                    pe.fillInStackTrace();
                }
                //If older than 3 hours
                if(forecast.calendar.getTimeInMillis() - now.getTimeInMillis() < -1000*60*60*3){
                    continue;
                }

                JSONObject main = obj.getJSONObject("main");
                forecast.temp = main.getDouble("temp");
                forecast.temp_min = main.getDouble("temp_min");
                forecast.temp_max = main.getDouble("temp_max");
                forecast.pressure = main.getDouble("pressure");
                forecast.pressure_sea_level = main.getDouble("sea_level");
                forecast.pressure_grnd_level = main.getDouble("grnd_level");
                forecast.humidity = main.getInt("humidity");

                // TODO: support for several weather conditions (https://openweathermap.org/weather-conditions) => parse if misty


                JSONObject weatherObj = obj.getJSONArray("weather").getJSONObject(0);
                forecast.weather_id = weatherObj.getInt("id");
                forecast.main = weatherObj.getString("main");
                forecast.description = weatherObj.getString("description");
                forecast.icon = weatherObj.getString("icon");

                forecast.clouds = obj.getJSONObject("clouds").getInt("all");
                forecast.wind_deg = obj.getJSONObject("wind").getDouble("deg");
                forecast.wind_speed = obj.getJSONObject("wind").getDouble("speed");


                int day = forecast.getCalendar().get(Calendar.DAY_OF_YEAR);
                if(day<minDay) minDay = day;
                int hour = forecast.getCalendar().get(Calendar.HOUR_OF_DAY);
                insertForecast(forecast, day - minDay, hour);
            }
            valid = true;
        } catch (JSONException e) {
            e.printStackTrace();
            valid = false;
        }
    }

    private void insertForecast(Forecast forecast, int day, int hour) {
        ForecastByHour fbh = forecastByDay.get(day);

        if(fbh == null){
            fbh = new ForecastByHour();
            fbh.put(hour, forecast);
            forecastByDay.put(day, fbh);
        }else{
            fbh.put(hour, forecast);
            forecastByDay.put(day, fbh);
        }


//        if(hour == 0 && day>now.get(Calendar.DAY_OF_YEAR)){
//            ForecastByHour last = forecastByDay.get(day-1);
//            if(last == null){
//
//                last = new ForecastByHour();
//                last.put(24, forecast);
//                forecastByDay.put(day-1, last);
//            }else {
//                last.put(24, forecast);
//            }
//        }
    }

    public DailyForecastMap getForecastByDay() {
        return forecastByDay;
    }

    public boolean isValid() {
        return valid;
    }

    public class Forecast implements Comparable<Forecast>{

        private Calendar calendar;
        private long dateUTC;
        private String dateString;
        private double temp;
        private double temp_min;
        private double temp_max;
        private double pressure;
        private double pressure_sea_level;
        private double pressure_grnd_level;
        private int humidity;

        private int weather_id;
        private String main;
        private String description;
        private String icon;

        private int clouds;
        private double wind_speed;
        private double wind_deg;

        public long getDateUtc() {
            return dateUTC;
        }

        public String getDateString() {
            return dateString;
        }

        public Calendar getCalendar(){ return calendar; }

        public double getTemperature() {
            return temp;
        }

        public double getMinTemperature() {
            return temp_min;
        }

        public double getMaxTemperature() {
            return temp_max;
        }

        public double getPressure() {
            return pressure;
        }

        public double getPressureSeaLevel() {
            return pressure_sea_level;
        }

        public double getPressureGroundLevel() {
            return pressure_grnd_level;
        }

        public int getHumidity() {
            return humidity;
        }

        public int getWeatherId() {
            return weather_id;
        }

        public String getWeatherName() {
            return main;
        }

        public String getWeatherDescription() {
            return description;
        }

        public String getWeatherIcon() {
            return icon;
        }

        public int getClouds() {
            return clouds;
        }

        public double getWind_speed() {
            return wind_speed;
        }

        public double getWind_deg() {
            return wind_deg;
        }


        @Override
        public int compareTo(@NonNull Forecast o) {
            return o.calendar.compareTo(o.calendar);
        }
    }
    public class Coordinates {
        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString() {
            return "lat=" + lat + "&lon=" + lon ;
        }

        public double lat;
        public double lon;
    }
    public class City{
        public String name;
        public int id;
        public Coordinates coord;
        public String country;
        City(JSONObject cityNode) throws JSONException{
            name = cityNode.getString("name");
            id = cityNode.getInt("id");
            country = cityNode.getString("country");
            JSONObject c = cityNode.getJSONObject("coord");
            coord = new Coordinates(c.getDouble("lat"), c.getDouble("lon"));
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(City.class) && name.equals(((City)obj).name);
        }
    }
}
