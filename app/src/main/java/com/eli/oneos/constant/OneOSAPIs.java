package com.eli.oneos.constant;

import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSInfo;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * OneSpace OS 4.x API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class OneOSAPIs {
    private static final String TAG = OneOSAPIs.class.getSimpleName();
    public static final int OneOS_UPLOAD_SOCKET_PORT = 7777;

    public static final String ONE_OS_PRIVATE_ROOT_DIR = "/";
    public static final String ONE_OS_PUBLIC_ROOT_DIR = "public/";
    public static final String ONE_OS_RECYCLE_ROOT_DIR = "/.recycle/";
    public static final String ONE_OS_SHARE_ROOT_DIR = "public/GUEST/";

    public static final String ONE_API_DEFAULT_PORT = "80";
    public static final String PREFIX_HTTP = "http://";
    public static final String ONE_API = "/oneapi";

    public static final String USER = ONE_API + "/user";

    public static final String NET_GET_MAC = ONE_API + "/net";

    public static final String FILE_API = ONE_API + "/file";
    public static final String FILE_UPLOAD = ONE_API + "/file/upload";
    public static final String FILE_THUMBNAIL = ONE_API + "/file/thumbnail";

    public static final String SYSTEM_SYS = ONE_API + "/sys";
    public static final String SYSTEM_SSUDP_CID = ONE_API + "/sys/ssudpcid";

    public static final String APP_API = ONE_API + "/app";
    public static final String BD_SUB = ONE_API + "/event/sub";
    public static final String GET_VERSION = "/ver.json";


    public static String genOpenUrl(LoginSession loginSession, OneOSFile file) {
        // http://192.168.1.17/home/admin/test.mp4?session=c5i6qqbe78oj0c1h78o0====
        if (isOneSpaceX1()) {
            String path = "storage/uid" + loginSession.getUserInfo().getUid() + file.getPath();
            if (file.getPath().indexOf("public") != -1) {
                path = "storage/" + file.getPath();
            }
            return loginSession.getUrl() + "/" + android.net.Uri.encode(path) + "?s=" + loginSession.getSession();
        } else {
            // http://ip/oneapi/file/download?session=xxx&path=/test.png
            String path = android.net.Uri.encode(file.getPath());
            return loginSession.getUrl() + OneOSAPIs.FILE_API + "/download?session=" + loginSession.getSession() + "&path=" + path;
        }
    }

    public static String genDownloadUrl(LoginSession loginSession, OneOSFile file) {
        // "http://192.168.1.17/oneapi/file/download?path=home%2Fadmin%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        //String path = android.net.Uri.encode(file.getAbsolutePath(loginSession.getUserInfo().getName()));

        if (isOneSpaceX1()) {
            String path = "storage/uid" + loginSession.getUserInfo().getUid() + file.getPath();
            if (file.getPath().indexOf("public") != -1) {
                path = "storage/" + file.getPath();
            }
            return loginSession.getUrl() + OneSpaceAPIs.FILE_DOWNLOAD + "?s=" + loginSession.getSession() + "&f=" + base64encode(path);
        } else {
            // http://ip/oneapi/file/download?session=xxx&path=/test.png
            String path = android.net.Uri.encode(file.getPath());
            return loginSession.getUrl() + OneOSAPIs.FILE_API + "/download?session=" + loginSession.getSession() + "&path=" + path;
        }
    }

    public static String genThumbnailUrl(LoginSession loginSession, OneOSFile file) {
        // "http://192.168.1.17/oneapi/file/thumbnail?path=%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        if (isOneSpaceX1()) {
            String path = "storage/uid" + loginSession.getUserInfo().getUid() + file.getPath();
            if (file.getPath().indexOf("public") != -1) {
                path = "storage/" + file.getPath();
            }
            return loginSession.getUrl() + OneSpaceAPIs.FILE_THUMBNAIL + "?s=" + loginSession.getSession() + "&f=" + base64encode(path);
        } else {
            String path = android.net.Uri.encode(file.getPath());
            return loginSession.getUrl() + OneOSAPIs.FILE_THUMBNAIL + "?session=" + loginSession.getSession() + "&path=" + path;
        }
    }

    public static String base64encode(String str) {
        try {
            byte[] encodeBase64 = Base64.encodeBase64(str.getBytes("UTF-8"));
            String strr = new String(encodeBase64);
            return (strr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static boolean isOneSpaceX1() {
        OneOSInfo info = LoginManage.getInstance().getLoginSession().getOneOSInfo();
        if (info == null) {
            return false;
        }
        String curOneOS = LoginManage.getInstance().getLoginSession().getOneOSInfo().getVersion();
        if (curOneOS == OneSpaceAPIs.ONESPACE_VER) return true;
        return false;

    }

    public static String getOneSpaceUid() {
        String uid = "storage/uid" + LoginManage.getInstance().getLoginSession().getUserInfo().getUid();
        return uid;
    }
}
