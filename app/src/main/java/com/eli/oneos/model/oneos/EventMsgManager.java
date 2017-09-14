package com.eli.oneos.model.oneos;

import android.util.Log;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by eli100 on 2017/3/22.
 */

public class EventMsgManager {
    private static final String TAG = EventMsgManager.class.getSimpleName();
    private static EventMsgManager INSTANCE = new EventMsgManager();
    private EventMsgThread eventMsgThread;
    private OnEventMsgListener listener;

    private EventMsgManager() {
    }

    public static EventMsgManager getInstance() {
        return INSTANCE;
    }

    public void setOnEventMsgListener(OnEventMsgListener listener) {
        this.listener = listener;
    }

    public void removeOnEventMsgListener(OnEventMsgListener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    public void startReceive() {
        if (null == eventMsgThread || !eventMsgThread.isAlive()) {
            eventMsgThread = new EventMsgThread();
            eventMsgThread.start();
            Log.d(TAG, "Start receive event msg...");
        }
    }

    public void onDestory() {
        if (null != eventMsgThread && eventMsgThread.isAlive()) {
            eventMsgThread.stopThread();
            Log.d(TAG, "Stop receive event msg...");
        }
    }



    private class EventMsgThread extends Thread {
        private LoginSession loginSession;
        @Override
        public void run() {
            BufferedReader in = null;
            try {

                loginSession = LoginManage.getInstance().getLoginSession();
                String ip = loginSession.getIp();
                String port = loginSession.getPort();
                //String url = "http://"+ip+":"+port+"/oneapi/event/sub";
                String url = "http://"+ip+":"+port+ OneOSAPIs.BD_SUB;
                Log.e(TAG, "Receive url: " + url);

                URL realUrl = new URL(url);
                URLConnection connection = realUrl.openConnection();
                connection.setRequestProperty("accept", "*/*");
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                connection.connect();
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                String line;
                while ((line = in.readLine()) != null) {
                    Log.e(TAG, "Receive msg: " + line);
                    line = line.replaceAll("\\s*|\t|\r|\n", "");
                    if (line.length() != 7 && line.length() != 0) {
                        Log.d(TAG,"line====="+line);
                        JSONObject json = new JSONObject(line.substring(5));
                        listener.onEventMsg(json);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "发送GET请求出现异常！" + e);
                e.printStackTrace();
                startReceive();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                Log.e(TAG, "--------------------------");
            }
        }

        public void stopThread() {
            interrupt();
        }
    }

    public interface OnEventMsgListener {
        void onEventMsg(JSONObject json);
    }
}
