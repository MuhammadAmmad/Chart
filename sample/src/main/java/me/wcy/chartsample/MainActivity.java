package me.wcy.chartsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.wcy.chart.data.GridData;
import me.wcy.chart.view.BarChart;
import me.wcy.chart.view.LineChart;
import me.wcy.chart.view.base.GridChart;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
        LineChart lineChart = (LineChart) findViewById(R.id.line_chart);
        LineChart lineChart2 = (LineChart) findViewById(R.id.line_chart2);
        BarChart barChart = (BarChart) findViewById(R.id.bar_chart);

        lineChart.setScrollView(scrollView);
        lineChart2.setScrollView(scrollView);
        barChart.setScrollView(scrollView);

        setChartData(lineChart);
        setChartData(lineChart2);
        setChartData(barChart);
    }

    private void setChartData(GridChart chart) {
        List<GridData> dataList = new ArrayList<>();
        dataList.add(randomData("1月"));
        dataList.add(randomData("2月"));
        dataList.add(randomData("3月"));
        dataList.add(randomData("4月"));
        dataList.add(randomData("5月"));
        dataList.add(randomData("6月"));
        dataList.add(randomData("7月"));
        dataList.add(randomData("8月"));
        dataList.add(randomData("9月"));
        dataList.add(randomData("10月"));
        dataList.add(randomData("11月"));
        dataList.add(randomData("12月"));

        chart.setDataList(dataList, true);
    }

    private GridData randomData(String title) {
        GridData.Entry[] entries = new GridData.Entry[3];
        String[] descs = new String[]{"2015年", "2016年", "2017年"};
        int[] colors = new int[]{0xFF7394E7, 0xFFF87FA9, 0xFF60D1AC};
        for (int i = 0; i < 3; i++) {
            entries[i] = new GridData.Entry(colors[i], descs[i], new Random().nextInt(101));
        }
        return new GridData(title, entries);
    }
}
