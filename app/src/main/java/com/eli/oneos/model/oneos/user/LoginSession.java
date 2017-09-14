package com.eli.oneos.model.oneos.user;

import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.oneos.OneOSInfo;

/**
 * User Login information
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class LoginSession {

    /**
     * User information
     */
    private UserInfo userInfo = null;
    /**
     * User settings
     */
    private UserSettings userSettings = null;
    /**
     * Login device information
     */
    private DeviceInfo deviceInfo = null;
    /**
     * Login session
     */
    private String session = null;
    /**
     * OneOS information
     */
    private OneOSInfo oneOSInfo = null;
    /**
     * Whether is new device to database
     */
    private boolean isNew = false;
    /**
     * Login timestamp
     */
    private long time = 0;

    public LoginSession(UserInfo userInfo, DeviceInfo deviceInfo, UserSettings userSettings, String session, boolean isNew, long time) {
        this.userInfo = userInfo;
        this.deviceInfo = deviceInfo;
        this.userSettings = userSettings;
        this.session = session;
        this.isNew = isNew;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public OneOSInfo getOneOSInfo() {
        return oneOSInfo;
    }

    public void setOneOSInfo(OneOSInfo oneOSInfo) {
        this.oneOSInfo = oneOSInfo;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getIp() {
        if (null == deviceInfo) {
            return null;
        }

        if (deviceInfo.getDomain() == Constants.DOMAIN_DEVICE_WAN) {
            return deviceInfo.getWanIp();
        }

        return deviceInfo.getLanIp();
    }

    public String getPort() {
        if (null == deviceInfo) {
            return null;
        }

        if (deviceInfo.getDomain() == Constants.DOMAIN_DEVICE_WAN) {
            return deviceInfo.getWanPort();
        }

        return deviceInfo.getLanPort();
    }

    /**
     * Formatted url, such as http://192.168.1.17:80
     *
     * @return Formatted url
     */
    public String getUrl() {
        if (null != deviceInfo) {
            String ip, port;
            if (deviceInfo.getDomain() == Constants.DOMAIN_DEVICE_WAN) {
                ip = deviceInfo.getWanIp();
                port = deviceInfo.getWanPort();
            } else {
                ip = deviceInfo.getLanIp();
                port = deviceInfo.getLanPort();
            }

            return OneOSAPIs.PREFIX_HTTP + ip + ":" + port;
        }

        return null;
    }

    /**
     * Whether the user is an administrator
     *
     * @return {@code true} if administrator, {@code false} otherwise.
     */
    public boolean isAdmin() {
        if (userInfo.getAdmin() == 1) {
            return true;
        }

        return false;
    }

    /**
     * Get user download save path
     *
     * @return Absolute path
     */
    public String getDownloadPath() {
        return userSettings.getDownloadPath();
    }

    /**
     * Whether LAN
     *
     * @return {@code true} if LAN, {@code false} otherwise.
     */
    public boolean isLANDevice() {
        return null == userInfo || userInfo.getDomain() == Constants.DOMAIN_DEVICE_LAN;
    }

    /**
     * Whether WAN
     *
     * @return {@code true} if WAN, {@code false} otherwise.
     */
    public boolean isWANDevice() {
        return null != userInfo && userInfo.getDomain() == Constants.DOMAIN_DEVICE_WAN;
    }

    /**
     * Whether SSUDP
     *
     * @return {@code true} if SSUDP, {@code false} otherwise.
     */
    public boolean isSSUDPDevice() {
        return null != userInfo && userInfo.getDomain() == Constants.DOMAIN_DEVICE_SSUDP;
    }
}
