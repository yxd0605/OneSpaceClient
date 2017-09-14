package com.eli.oneos.model.oneos.user;

import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.UserInfo;

/**
 * Used to manage user login information.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/11.
 */
public class LoginManage {
    private LoginSession loginSession = null;
    /**
     * Singleton instance of {@link LoginManage}
     */
    private static LoginManage INSTANCE = new LoginManage();

    /**
     * Get singleton instance of the class.
     *
     * @return {@link LoginManage} Instance
     */
    public static LoginManage getInstance() {
        return LoginManage.INSTANCE;
    }

    private LoginManage() {
    }

    /**
     * Is logged OneSpace
     *
     * @return {@code true} if logged, {@code false} otherwise
     */
    public boolean isLogin() {
        if (loginSession == null) {
            return false;
        }

        if (loginSession.getUserInfo() == null || loginSession.getDeviceInfo() == null) {
            return false;
        }

        if (loginSession.getSession() == null) {
            return false;
        }

        return true;
    }

    /**
     * Logout OneSpace
     *
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean logout() {
        if (isLogin()) {
            UserInfo info = loginSession.getUserInfo();
            info.setIsLogout(true);
            UserInfoKeeper.update(info);

            loginSession.setSession(null);
            return true;
        }

        return false;
    }

    /**
     * Save User {@link LoginSession} Information
     * <p/>
     * You should update it only when the user login successful, and it will be saved for later use.
     *
     * @param loginSession {@link LoginSession}
     */
    public void setLoginSession(LoginSession loginSession) {
        this.loginSession = loginSession;
    }

    /**
     * Get the saved {@link LoginSession}
     *
     * @return {@link LoginSession}
     */
    public LoginSession getLoginSession() {
        return this.loginSession;
    }


    public boolean isHttp() {
        return null == loginSession || loginSession.isLANDevice() || loginSession.isWANDevice();
    }
}
