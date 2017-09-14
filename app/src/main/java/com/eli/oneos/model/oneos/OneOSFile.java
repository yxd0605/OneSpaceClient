package com.eli.oneos.model.oneos;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;

import java.io.Serializable;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class OneOSFile implements Serializable {
    private static final long serialVersionUID = 11181567L;

    // for sticky header
    private int section = 0;
    // {"perm":"rwxr-xr-x","type":"audio","toPath":"haizeiw .mp3","gid":0,"path":"\/haizeiw .mp3","uid":1001,"time":1187168313,"size":6137050}
    private String path = null;
    private String perm = null;
    /**
     * File Type
     * <p/>
     * Server file type table:
     * <p/>
     * { directory="dir",jpg="pic",gif="pic",png="pic",jpeg="pic",bmp="pic",
     * <p/>
     * mp3="audio",ogg="audio",wav="audio",flac="audio", wma="audio",m4a="audio",
     * <p/>
     * avi="video",mp4="video",flv="video",rmvb="video",mkv="video",mov="video",
     * <p/>
     * wmv="video", mpg="video",doc="doc",xls="doc",ppt="doc",docx="doc",xlsx="doc",
     * <p/>
     * pptx="doc",pdf="doc",txt="doc",csv="doc",tea="enc" }
     */
    private String type = null;
    private String name = null;
    private int gid = 0;
    private int uid = 0;
    private long time = 0;
    private long size = 0;
    private long month = 0;
    private int actProgress = 0;
    private int encrypt = 0;
    private long phototime = 0;
    private long udtime = 0;
    private long cttime = 0;
    private String fullpath = null;


    private String fmtUDTime = null;
    private String fmtCTTime = null;
    /**
     * file shown icon
     */
    private int icon = R.drawable.icon_file_default;
    /**
     * formatted file time
     */
    private String fmtTime = null;
    /**
     * formatted file size
     */
    private String fmtSize = null;

    /**
     * OneOS File absolute path
     *
     * @param user user name
     * @return private file: [/home/user/path], public file: [path]
     */
    public String getAbsolutePath(String user) {
        if (isPublicFile()) {
            return path;
        } else {
            return "home/" + user + path;
        }
    }

    public boolean isOwner(int uid) {
        return this.uid == uid;
    }

    public boolean isGroupRead() {
        if (perm == null || perm.length() != 9) {
            return false;
        }

        return perm.charAt(3) == 'r';
    }

    public boolean isGroupWrite() {
        if (perm == null || perm.length() != 9) {
            return false;
        }

        return perm.charAt(4) == 'w';
    }

    public boolean isOtherRead() {
        if (perm == null || perm.length() != 9) {
            return false;
        }

        return perm.charAt(6) == 'r';
    }

    public boolean isOtherWrite() {
        if (perm == null || perm.length() != 9) {
            return false;
        }

        return perm.charAt(7) == 'w';
    }

    public String getPath() {
        if (OneOSAPIs.isOneSpaceX1() && isEncrypt()){
            return path+".TEA";
        }
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPerm() {
        return perm;
    }

    public void setPerm(String perm) {
        this.perm = perm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getFmtSize() {
        return fmtSize;
    }

    public void setFmtSize(String fmtSize) {
        this.fmtSize = fmtSize;
    }

    public int getProgress(){return actProgress; }
    public void setProgress(int progress){
        this.actProgress = progress;
    }

    public void setEncrypt(int encr) {this.encrypt = encr;}
    public boolean isEncr() { return this.encrypt == 1;}

    public void setUdtime(long udtime){
        this.udtime = udtime;
    }
    public long getUdtime(){
        return udtime;
    }

    public void setCttime(long cttime){
        this.cttime = cttime;
    }
    public long getCttime(){
        return cttime;
    }

    public String getFmtTime() {
        return fmtTime;
    }
    public void setFmtTime(String fmtTime) {
        this.fmtTime = fmtTime;
    }

    public String getFmtUDTime() {
        return fmtUDTime;
    }
    public void setFmtUDTime(String fmtUDTime) {
        this.fmtUDTime = fmtUDTime;
    }

    public String getFmtCTTime() {
        return fmtCTTime;
    }

    public void setFmtCTTime(String fmtCTTime) {
        this.fmtCTTime = fmtCTTime;
    }




    public boolean isPicture() {
        return null != this.type && this.type.equalsIgnoreCase("pic");
    }

    public boolean isVideo(){
        return null != this.type && this.type.equalsIgnoreCase("video");
    }

    public boolean isGif() {
        return null != this.name && this.name.toLowerCase().endsWith(".gif");
    }

    public boolean isDocument() { return null != this.type && this.type.equalsIgnoreCase("doc"); }

    public  boolean isExtract() { return null != this.name && (this.name.toLowerCase().endsWith(".zip")
            || this.name.toLowerCase().endsWith(".rar") || this.name.toLowerCase().endsWith(".tgz")
            || this.name.toLowerCase().endsWith(".nz2") || this.name.toLowerCase().endsWith(".bz")
            || this.name.toLowerCase().endsWith(".gz") || this.name.toLowerCase().endsWith(".7z")
            || this.name.toLowerCase().endsWith(".jar"));}

    public boolean isEncrypt() {
        if (OneOSAPIs.isOneSpaceX1()){
            return this.encrypt == 1;
        }else {
            return null != this.type && this.type.equalsIgnoreCase("enc");
        }
    }

    public boolean isDirectory() {
        return null != this.type && this.type.equalsIgnoreCase("dir");
    }

    public boolean isPublicFile() {
        return this.path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR);
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public long getMonth() {
        return month;
    }

    public void setMonth(long month) {
        this.month = month;
    }

    public void setPhototime (long phototime){ this.phototime = phototime; }
    public long getPhototime (){
        return phototime;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof OneOSFile) {
            OneOSFile file = (OneOSFile) other;
            return this.path.equals(file.path);
        }

        return false;
    }

    public void setFullpath(String fullpath){ this.fullpath = fullpath; }

    public String  getFullpath(){
        return fullpath;
    }

    @Override
    public String toString() {
        if (OneOSAPIs.isOneSpaceX1()){
            return "OneOSFile:{name:\"" + name + "\", path:\"" + path + "\", uid:\"" + uid + "\", type:\"" + type
                    + "\", size:\"" + fmtSize + "\", time:\"" + time + "\", perm:\"" + perm + "\", gid:\"" + gid
                    + "\", enc:\"" + encrypt + "\", phototime:\"" + phototime + "\",fullpath:\""+ fullpath + "\"}";
        } else {
            return "OneOSFile:{name:\"" + name + "\", path:\"" + path + "\", uid:\"" + uid + "\", type:\"" + type
                    + "\", size:\"" + fmtSize + "\", time:\"" + fmtTime + "\", perm:\"" + perm + "\", gid:\"" + gid +
                    "\", month:\"" + month +"\", cttime:\"" + cttime +"\", udtime:\"" + udtime + "\"}";
        }
    }
}
