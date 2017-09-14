package com.eli.oneos.model.upgrade;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/11/4.
 */

public class AppVersionInfo {
    private String name = null;
    private String version = null;
    private String link = null;
    private String time = null;
    private String oneOs = null;
    private ArrayList<String> logs = null;

    public AppVersionInfo(String name, String version, String link, String time, String oneos, ArrayList<String> logs) {
        this.name = name;
        this.version = version;
        this.link = link;
        this.time = time;
        this.oneOs = oneos;
        this.logs = logs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOneOs() {
        return oneOs;
    }

    public void setOneOs(String oneos) {
        this.oneOs = oneos;
    }

    public ArrayList<String> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<String> logs) {
        this.logs = logs;
    }
}
