package com.example.pureweather.gson;

import com.google.gson.annotations.SerializedName;

public class Daily_Forecast {

    public String date;

    public String tmp_max;
    public String tmp_min;

    @SerializedName("cond_txt_d")
    public String day_info;

}
