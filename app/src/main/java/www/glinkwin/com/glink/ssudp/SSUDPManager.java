package www.glinkwin.com.glink.ssudp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.eli.oneos.MyApplication;


public class SSUDPManager {
    private static final String TAG = SSUDPManager.class.getSimpleName();

    private static SSUDPManager instance = null;
    //    private ArrayList<SSUDPRequest> ssudpRequests = new ArrayList<>();
    private SSUDPRequest ssudpRequest;
    private SSUDPDownload ssudpDownload;
    private SSUDPUpload ssudpUpload;
    private SSUDPRequest.OnSSUdpResponseListener respCallback;
    private SSUDPRequest.OnSSUdpStateListener stateListener = new SSUDPRequest.OnSSUdpStateListener() {
        @Override
        public void onStateChanged(boolean isConnected) {
            Intent intent = new Intent(SSUDPConst.BROADCAST_ACTION_SSUDP);
            intent.putExtra(SSUDPConst.EXTRA_SSUDP_STATE, isConnected);
            MyApplication.getAppContext().sendBroadcast(intent);
        }
    };

    private SSUDPManager() {
    }

    public static SSUDPManager getInstance() {
        if (null == instance) {
            instance = new SSUDPManager();
        }

        return instance;
    }

    public void initSSUDPClient(Context context, String strCid, String strPwd) {
//        if (ssudpRequests.size() >= SSUDPConst.MAX_CLIENTS_COUNT) {
//            Log.e(TAG, String.format("SSUDP client can create up to %d.", SSUDPConst.MAX_CLIENTS_COUNT));
//            return;
//        }
//
//        int count = SSUDPConst.MAX_CLIENTS_COUNT - ssudpRequests.size();
//        for (int i = 0; i < count; i++) {
//            SSUDPRequest client = new SSUDPRequest(context, strCid, strPwd);
//            client.setOnSSUdpStateListener(stateListener);
//            ssudpRequests.add(client);
//        }

        if (null != ssudpRequest) {
            if (ssudpRequest.config.strcid.equals(strCid)) {
                Log.d(TAG, "SSUDP has been initialized");
                return;
            } else {
                ssudpRequest.stopReceivedThread();
                ssudpRequest = null;
            }
        }

        ssudpRequest = new SSUDPRequest(context, strCid, strPwd);
        ssudpRequest.setOnSSUdpStateListener(stateListener);
        ssudpDownload = new SSUDPDownload(context, strCid, strPwd);
        ssudpUpload = new SSUDPUpload(context, strCid, strPwd);

        Log.d(TAG, String.format("Init SSUDP client complete: %d.", SSUDPConst.MAX_CLIENTS_COUNT));
    }

    public void destroy() {
        destroySSUDPClient();
        ssudpDownload.destroy();
        ssudpDownload = null;
        ssudpUpload.destroy();
        ssudpUpload = null;
    }

    private int times = 0;

    public boolean connectSSUPDClient(final OnSSUDPConnectListener listener) {
//        if (ssudpRequests.isEmpty()) {
//            Log.e(TAG, "None SSUDP client to connect.");
//            return false;
//        }

        times = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    times++;
                    boolean allConnected = true;
//                    for (SSUDPRequest client : ssudpRequests) {
//                        if (!client.isConnected() && client.connect()) {
//                            allConnected = false;
//                        }
//                    }

                    if (ssudpRequest.isConnected() || ssudpRequest.connect()) {
                        if (null != listener) {
                            listener.onResult(times, true);
                        }
                        break;
                    }

                    if (times == 5) {
                        listener.onResult(times, false);
                        break;
                    }
                }
            }
        }).start();

        return true;
    }

    public void startSSUDPClient() {
//        for (SSUDPRequest client : ssudpRequests) {
//            client.startReceivedThread();
//        }
        if (null != ssudpRequest) {
            ssudpRequest.startReceivedThread();
        }
    }

    public void destroySSUDPClient() {
//        for (SSUDPRequest client : ssudpRequests) {
//            client.stopReceivedThread();
//        }
//        ssudpRequests.clear();
        if (null != ssudpRequest) {
            ssudpRequest.stopReceivedThread();
            ssudpRequest = null;
        }
    }

    public boolean sendSSUDPRequest(String request, SSUDPRequest.OnSSUdpResponseListener listener) {
        this.respCallback = listener;
//        if (ssudpRequests.isEmpty()) {
//            Log.e(TAG, "None SSUDP client to use.");
//            if (null != respCallback) {
//                respCallback.onFailure(SSUDPConst.SSUDP_ERROR_NOT_READY, "None SSUDP client to use, waiting...");
//            }
//
//            return false;
//        }
//        SSUDPRequest client = ssudpRequests.get(0);
//        client.send(SSUDPConst.TAGID_FTP_REQUEST, request, respCallback);

        ssudpRequest.send(SSUDPConst.TAGID_FTP_REQUEST, request, respCallback);

        return true;
    }

    public SSUDPFileChunk ssudpDownloadRequest(SSUDPFileChunk chunk) {
        return ssudpDownload.download(chunk);
    }

    public SSUDPFileChunk ssudpUploadRequest(SSUDPFileChunk chunk) {
        return ssudpUpload.upload(chunk);
    }

    public interface OnSSUDPConnectListener {
        void onResult(int progress, boolean connected);
    }
}
