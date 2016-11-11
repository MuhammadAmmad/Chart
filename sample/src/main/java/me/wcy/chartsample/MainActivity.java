package me.wcy.chartsample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.wcy.chart.data.GridData;
import me.wcy.chart.view.BarChart;
import me.wcy.chart.view.LineChart;

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

        setLineChartData(lineChart);
        setLineChartData(lineChart2);
        setBarChartData(barChart);
    }

    private void setLineChartData(LineChart lineChart) {
        List<GridData> dataList = new ArrayList<>();
        dataList.add(randomData("魔神"));
        dataList.add(randomData("剑圣"));
        dataList.add(randomData("鬼泣"));
        dataList.add(randomData("修罗"));
        dataList.add(randomData("忍者"));
        dataList.add(randomData("百花"));
        dataList.add(randomData("驱魔"));

        lineChart.setDataList(dataList, true);
    }

    private void setBarChartData(BarChart barChart) {
        List<GridData> dataList = new ArrayList<>();
        dataList.add(randomData("魔神"));
        dataList.add(randomData("剑圣"));
        dataList.add(randomData("鬼泣"));
        dataList.add(randomData("修罗"));
        dataList.add(randomData("忍者"));
        dataList.add(randomData("百花"));
        dataList.add(randomData("驱魔"));

        barChart.setDataList(dataList, true);
    }

    private GridData randomData(String title) {
        GridData.Entry[] entries = new GridData.Entry[3];
        String[] descs = new String[]{"力量", "智力", "体力"};
        int[] colors = new int[]{Color.RED, Color.BLUE, Color.YELLOW};
        for (int i = 0; i < 3; i++) {
            entries[i] = new GridData.Entry(colors[i], descs[i], new Random().nextInt(101));
        }
        return new GridData(title, entries);
    }
}
