package www.glinkwin.com.glink.ssudp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by gaoyun@eli-tech.com on 2016/6/15.
 */
public class SSUDPRequest extends SSUDPClient {
    private static final String TAG = SSUDPRequest.class.getSimpleName();

    private OnSSUdpResponseListener responseListener;
    private OnSSUdpStateListener stateListener;

    private Thread eventThread = null;
    private Thread streamThread = null;
    private Thread channelThread = null;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SSUDPConst.SSUDP_MSG_EVENT:
//                    if (null != responseListener) {
//                        responseListener.onEventReceived(msg.arg1, msg.arg2);
//                    }
                    break;
                case SSUDPConst.SSUDP_MSG_CHANNEL:
//                    if (null != responseListener) {
//                        responseListener.onChannelReceived((Byte) msg.obj);
//                    }
                    break;
                case SSUDPConst.SSUDP_MSG_STREAM:
                    isIdle = true;
                    if (null != responseListener) {
                        boolean success = (Boolean) msg.obj;
                        if (success) {
                            responseListener.onSuccess(stream.header, stream.buffer);
                        } else {
                            responseListener.onFailure(msg.arg1, "SSUDP send failed.");
                        }
                    }
                    stream.resetBuffer();
                    stream.resetHeader();
                    break;
                case SSUDPConst.SSUDP_MSG_STATE:
                    boolean conn = (Boolean) msg.obj;
                    if (!conn) {
                        isIdle = true;
                        if (null != responseListener) {
                            responseListener.onFailure(SSUDPConst.SSUDP_ERROR_DISCONNECT, "SSUDP disconnected.");
                        }
                    }
                    if (null != stateListener) {
                        stateListener.onStateChanged(conn);
                    }
                    break;
            }
        }
    };

    public SSUDPRequest(Context ctx, String cid, String pwd) {
        super(ctx, cid, pwd);
    }

    @Override
    public boolean connect() {
        initNetMask();

        if (clientId == 0) {
            updateState(false);
        } else {
            pcsLink = ssudpConnect(clientId);

            if (pcsLink != 0) {
                delayMS(100);
                sendUUID();
                isIdle = true;
                updateState(true);
            } else {
                updateState(false);
            }
            Log.d(TAG, config.id() + "---- Connect result: " + isConnected);
        }

        return isConnected;
    }

    @Override
    public void disconnect() {
        if (pcsLink != 0) {
            ssudpDisconnect(pcsLink);
            updateState(false);
            delayMS(300);
            ssudpClose(pcsLink);
            delayMS(600);
            pcsLink = 0;
        }
    }

    @Override
    public void closeClient() {
        if (clientId != 0) {
            ssudpCloseClient(clientId);
            clientId = 0;
            updateState(false);
        }
    }

    public boolean startReceivedThread() {
//        startReceiveEventThread();
//        startReceiveChannelThread();
        startReceiveStreamThread();
        Log.e(TAG, config.id() + "---- All thread started...");

        return true;
    }

    public void stopReceivedThread() {
        isExit = true;
        if (null != eventThread && eventThread.isAlive()) {
            eventThread.interrupt();
            eventThread = null;
        }
        if (null != channelThread && channelThread.isAlive()) {
            channelThread.interrupt();
            channelThread = null;
        }
        if (null != streamThread && streamThread.isAlive()) {
            streamThread.interrupt();
            streamThread = null;
        }

        disconnect();
        closeClient();
    }

//    /**
//     * Receive Event Message thread
//     **/
//    private void startReceiveEventThread() {
//        if (null == eventThread || !eventThread.isAlive()) {
//            eventThread = new Thread() {
//                public void run() {
//                    Log.d(TAG, config.id() + "Receive Event Message thread running...");
//                    byte[] buffer = new byte[8];
//                    while (!isExit) {
//                        if (clientEvent(buffer)) {
//                            int type = SSUDPType.byte2int(buffer, 0);
//                            int code = SSUDPType.byte2int(buffer, 4);
//
//                            Message msg = Message.obtain();
//                            msg.what = SSUDPConst.SSUDP_MSG_EVENT;
//                            msg.arg1 = type;
//                            msg.arg2 = code;
//                            handler.sendMessage(msg);
//                        }
//                    }
//
//                    Log.d(TAG, config.id() + "Receive Event Message thread stopped...");
//                }
//            };
//            eventThread.start();
//        }
//    }
//
//    /**
//     * Receive Channel Message thread
//     **/
//    private void startReceiveChannelThread() {
//        if (null == channelThread || !channelThread.isAlive()) {
//            channelThread = new Thread() {
//                public void run() {
//                    Log.d(TAG, config.id() + "Receive Channel Message thread running...");
//                    while (!isExit) {
//                        int lens = udpReceive();
//                        if (lens < 0) {
//                            break;
//                        }
//                        if (lens <= 0) {
//                            continue;
//                        }
//
//                        Message msg = Message.obtain();
//                        msg.what = SSUDPConst.SSUDP_MSG_CHANNEL;
//                        msg.obj = packet.channel;
//                        handler.sendMessage(msg);
//                    }
//                    Log.d(TAG, config.id() + "Receive Channel Message thread stopped...");
//                }
//            };
//            channelThread.start();
//        }
//    }

    /**
     * Receive Stream Message thread
     **/
    private void startReceiveStreamThread() {
        if (null == streamThread || !streamThread.isAlive()) {
            streamThread = new Thread() {
                public void run() {
                    Log.d(TAG, config.id() + "Receive Stream Message thread running...");
                    while (!isExit) {
                        if (!isConnected()) {
                            if (!connect()) {
                                delayMS(3000);
                            }
                        } else {
                            int ret = pollingStream();
                            if (ret == -1) {
                                disconnect();
                            } else {
                                if (stream.type == SSUDPConst.TAGID_FTP_REQUEST_ACK) {
                                    Message msg = Message.obtain();
                                    msg.what = SSUDPConst.SSUDP_MSG_STREAM;
                                    msg.obj = true;
                                    handler.sendMessage(msg);
                                }
                            }
                        }
                    }

                    Log.d(TAG, config.id() + "Receive Stream Message thread stopped...");
                }
            };
            streamThread.start();
        }
    }

    public void send(int cmd, String request, OnSSUdpResponseListener listener) {
        this.responseListener = listener;
        byte[] reqBytes = request.getBytes();
        int msgLen = reqBytes.length;
        byte buffer[] = new byte[4 + msgLen + 1];
        SSUDPType.int2byte(cmd | ((msgLen + 1) << 8), buffer, 0);
        System.arraycopy(reqBytes, 0, buffer, 4, msgLen);
        Log.d(TAG, config.id() + ">>>> Send request: " + request);
        int send = send(buffer, buffer.length, SSUDPConst.SS_WAIT);
        if (send < 0) {
            Log.d(TAG, config.id() + ">>>> Send failed, result=" + send + "; connected=" + isConnected());
            Message msg = Message.obtain();
            msg.what = SSUDPConst.SSUDP_MSG_STREAM;
            msg.obj = false;
            msg.arg1 = send;
            handler.sendMessage(msg);
        }
    }

    @Override
    synchronized int send(byte[] buffer, int len, int flags) {
        if (pcsLink == 0) {
            return SSUDPConst.SSUDP_ERROR_NOT_READY;
        }

        if (!isConnected()) {
            return SSUDPConst.SSUDP_ERROR_DISCONNECT;
        }

        if (!isIdle()) {
            return SSUDPConst.SSUDP_ERROR_IS_BUSY;
        }

        isIdle = false;
        len = ssudpSend(pcsLink, buffer, len, flags);
        if (len == -1) {
            updateState(false);
            isIdle = true;
            return SSUDPConst.SSUDP_ERROR_DISCONNECT;
        }

        return len;
    }

    private int udpReceive() {
        /*
        * 0 :udp channel
        * 1 :udp data
        */
        int lens = ssudpUdpRecv(clientId, packet.buffer, packet.buffer.length, SSUDPConst.SS_WAIT);
        if (lens > 0) {
            Log.d(TAG, config.id() + "Udp received len: " + lens);
            // byte[] databuf = new byte[512];
            // databuf[0] is channel
            // databuf[1~12] rev
            // databuf[13] user data start
            packet.channel = packet.buffer[0];
            packet.data_offset = 13;
            packet.data_length = lens - 13;
            packet.length = lens;

            return lens;
        }

        return 0;
    }

    private int receive(byte[] buffer, int offset, int lens, int flags) {
        if (pcsLink == 0) {
            return -1;
        }

        lens = ssudpRecv(pcsLink, buffer, offset, lens, flags);
        // Log.d(TAG, "Received len: " + lens);
        if (lens == -1) {
            updateState(false);
        }

        return lens;
    }

    private int pollingStream() {
        int ret;
        int rdLen = 4;
        int rdPos = 0;

        // read header 4bytes: 0 stream type, 3bytes: data length;
        while (rdLen != 0) {
            ret = receive(stream.header, rdPos, rdLen, SSUDPConst.SS_WAIT);
            if (ret < 0) {
                return -1;
            }

            rdLen -= ret;
            rdPos += ret;
        }

        stream.length = SSUDPType.byte2int(stream.header, 0) >> 8;
        stream.type = stream.header[0];
        stream.buffer = new byte[stream.length];
        if (stream.length > 0) {
            rdLen = stream.length;
            rdPos = 0;

            while (rdLen != 0) {
                ret = receive(stream.buffer, rdPos, rdLen, SSUDPConst.SS_WAIT);
                if (ret < 0) {
                    return -1;
                }

                rdLen -= ret;
                rdPos += ret;
            }
        }

        return stream.length;
    }

    private boolean clientEvent(byte[] buffer) {
        int ret = ssudpGetEvent(clientId, buffer, 8, SSUDPConst.SS_WAIT);
        /// Log.e("clientEvent:"+ret,"obj:"+gObj);
        if (8 == ret) {
            return true;
        }
        return false;
    }

    private void updateState(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            Message msg = Message.obtain();
            msg.what = SSUDPConst.SSUDP_MSG_STATE;
            msg.obj = isConnected;
            handler.sendMessage(msg);
        }
    }

    public void setOnSSUdpStateListener(OnSSUdpStateListener listener) {
        this.stateListener = listener;
    }

    public interface OnSSUdpResponseListener {
        void onSuccess(byte[] header, byte[] body);

        void onFailure(int errno, String errMsg);
    }

    public interface OnSSUdpStateListener {
        void onStateChanged(boolean isConnected);
    }

}
