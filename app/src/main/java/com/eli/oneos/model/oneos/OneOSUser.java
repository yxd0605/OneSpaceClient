package com.eli.oneos.model.oneos;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/24.
 */
public class OneOSUser {
    private String name = null;
    private int uid = 0;
    private int gid = 0;
    private long used = 0;
    private long space = 0;
    private JSONArray agids ;

    public OneOSUser(String name, int uid, int gid, JSONArray agids) {
        this.name = name;
        this.uid = uid;
        this.gid = gid;
        this.agids = agids;
    }
    public JSONArray getAgids(){
        return agids;
    }
    public void setAgids( JSONArray agids ){
        this.agids = agids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public long getSpace() {
        return space;
    }

    public void setSpace(long space) {
        this.space = space;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }
}
