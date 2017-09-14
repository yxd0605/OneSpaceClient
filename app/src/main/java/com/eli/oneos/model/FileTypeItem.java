package com.eli.oneos.model;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/25.
 */
public class FileTypeItem {
    private int title = 0;
    private int normalIcon = 0;
    private int pressedIcon = 0;
    private Object flag = null;

    public FileTypeItem(int title, int norIcon, int preIcon, Object flag) {
        this.normalIcon = norIcon;
        this.title = title;
        this.pressedIcon = preIcon;
        this.flag = flag;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getNormalIcon() {
        return normalIcon;
    }

    public void setNormalIcon(int normalIcon) {
        this.normalIcon = normalIcon;
    }

    public int getPressedIcon() {
        return pressedIcon;
    }

    public void setPressedIcon(int pressedIcon) {
        this.pressedIcon = pressedIcon;
    }

    public Object getFlag() {
        return flag;
    }

    public void setFlag(Object flag) {
        this.flag = flag;
    }
}
