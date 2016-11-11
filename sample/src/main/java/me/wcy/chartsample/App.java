package me.wcy.chartsample;

import android.app.Application;

import me.wcy.chart.ChartUtils;

/**
 * Created by hzwangchenyan on 2016/11/10.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ChartUtils.init(this);
    }
}
