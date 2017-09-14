package com.eli.oneos.widget.sticky;

/**
 * Created by Administrator on 2016/2/3.
 */
public class StickyTimeSection {
    private int index = 0;
    private long time = 0;

    public StickyTimeSection() {
    }

    public StickyTimeSection(int index, long time) {
        this.index = index;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getTime() {
        return time;
    }

    public String getTimeLable() {
        return String.valueOf(time);
    }

    public void setTime(long time) {
        this.time = time;
    }
}
