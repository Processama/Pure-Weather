package com.example.pureweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pureweather.gson.Forecast;
import com.example.pureweather.gson.Weather;
import com.example.pureweather.service.AutoUpdateService;
import com.example.pureweather.util.HttpUtil;
import com.example.pureweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public String weatherId;

    private ImageView backimage;
    private int img;
    public DrawerLayout drawerLayout;
    private Button navButton;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //使状态栏和界面配合
        View decoView = getWindow().getDecorView();
        decoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_weather);

        //背景
        backimage = (ImageView) findViewById(R.id.back_image);

        //滑动菜单
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        //下拉刷新
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);

        //各信息布局控件
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        //获取缓存
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);

        //判断有无缓存
        if(weatherString!=null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            ShowWeatherInfo(weather);
        }else{
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

    }

    //发送http请求获取json数据并解析
    public void requestWeather(String weatherId){
        String WeatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId;
        HttpUtil.sendOkHttpRequest(WeatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            ShowWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        swipeRefresh.setRefreshing(false);
    }

    //给布局赋值
    public void ShowWeatherInfo(Weather weather){
        String cityname = weather.basic.cityName;
        String updateTime = "更新时间："+weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"°C";
        int deg = Integer.parseInt(weather.now.temperature);
        if(deg>30){
            img = R.drawable.toohot;
        }else if(deg>20){
            img = R.drawable.hot;
        }else if(deg>10){
            img = R.drawable.normal;
        }else if(deg>0){
            img = R.drawable.lesscold;
        }else{
            img = R.drawable.cold;
        }
        backimage.setImageResource(img);
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityname);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carwash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carwash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        //后台服务定时更新数据
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
