package me.wcy.chart.data;

public class GridData {
    private final String title;

    private final Entry[] entries;

    public GridData(String title, Entry[] entries) {
        this.title = title;
        this.entries = entries;
    }

    public String getTitle() {
        return title;
    }

    public Entry[] getEntries() {
        return entries;
    }

    public float getMaxValue() {
        float max = 0;
        for (Entry entry : entries) {
            max = Math.max(max, entry.getValue());
        }
        return max;
    }

    public static class Entry {
        // mutable
        private float value;

        // immutable
        private final int lineColor;
        private final String desc;

        public Entry(int color, String desc, float value) {
            this.lineColor = color;
            this.desc = desc;

            this.value = value;
        }

        public int getLineColor() {
            return lineColor;
        }

        public String getDesc() {
            return desc;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }
}
