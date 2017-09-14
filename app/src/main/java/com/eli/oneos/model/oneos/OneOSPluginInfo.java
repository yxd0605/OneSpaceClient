package com.eli.oneos.model.oneos;


import com.eli.oneos.constant.OneOSAPIs;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * OneSpace plugin information
 *
 * @author shz
 * @since V 1.6.21
 */
public class OneOSPluginInfo {
    public enum State {
        GETTING,
        ON,
        OFF,
        UNKNOWN
    }

    private String pack = null;
    private String version = null;
    private String name = null;
    private boolean canDel = false;
    private boolean canStat = false;
    private boolean canOff = false;
    private State stat = State.GETTING;
    private String logo = null;
    private String url = null;

    public OneOSPluginInfo(JSONObject jsonObj) {
        if (null != jsonObj) {
            try {
                this.pack = jsonObj.getString("pack");
                this.name = jsonObj.getString("name");
                this.version = jsonObj.getString("ver");
                this.canDel = jsonObj.getBoolean("candel");
                this.logo = jsonObj.getString("logo");
                this.canStat = jsonObj.getBoolean("canstat");
                this.canOff = jsonObj.getBoolean("canoff");
                if (OneOSAPIs.isOneSpaceX1()) {
                    this.stat = jsonObj.getString("stat").equalsIgnoreCase("on") ? State.ON : State.OFF;
                    //this.stat = jsonObj.getString("stat");
                }
                this.url = jsonObj.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCanDel() {
        return canDel;
    }

    public void setCanDel(boolean canDel) {
        this.canDel = canDel;
    }

    public boolean isCanStat() {
        return canStat;
    }

    public void setCanStat(boolean canStat) {
        this.canStat = canStat;
    }

    public boolean isCanOff() {
        return canOff;
    }

    public void setCanOff(boolean canOff) {
        this.canOff = canOff;
    }

    public State getStat() {
        return stat;
    }

    public void setStat(State stat) {
        this.stat = stat;
    }

    /**
     * If the plugin has been opened
     *
     * @return if opened
     */
    public boolean isOn() {
        return (this.stat == State.ON);
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
