package www.glinkwin.com.glink.ssudp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Random;

/**
 * Created by gaoyun@eli-tech.com on 2016/6/15.
 */
public abstract class SSUDPClient {
    private static final String TAG = SSUDPClient.class.getSimpleName();

    static {
        System.loadLibrary("ssudp");
    }

    public class Stream {
        /**
         * Stream Type
         */
        public byte type;
        /**
         * Data length, except stream header (4 bytes)
         */
        public int length;
        /**
         * Stream header bytes (4 bytes)
         */
        public byte[] header;
        /**
         * Stream body bytes
         */
        public byte[] buffer;

        public void resetBuffer() {
            buffer = null;
        }

        public void resetHeader() {
            for (int i = 0; i < header.length; i++) {
                header[i] = 0;
            }
        }

        private Stream() {
//            buffer = new byte[1024 * 1024];
            header = new byte[4];
            length = 0;
        }
    }

    public class Packet {
        public byte channel;
        public int data_offset;
        public int data_length;
        public int length;
        public byte[] buffer;

        private Packet() {
            buffer = new byte[4096];
            length = 0;
        }
    }

    /*********************************/
    //native interface
    public native int ssudpSetRoute(int pobj, int ip, int gw, int mask);

    public native int ssudpGetEvent(int pobj, byte[] buffer, int lens, int flags);

    public native int ssudpUdpRecv(int pcs, byte[] buffer, int lens, int flags);

    public native int ssudpHelp();

    public native int ssudpNewClient(ClientConfig cfg);

    public native int ssudpConnect(int pobj);

    public native int ssudpClose(int pcs);

    public native int ssudpDisconnect(int pcs);/*断开连接，不释放资源*/

    public native int ssudpCloseClient(int pobj);/*释放资源*/

    public native int ssudpSend(int pcs, byte[] buffer, int lens, int flags);

    public native int ssudpRecv(int pcs, byte[] buffer, int offset, int lens, int flags);

    public native int ssudpGetLinkSts(int pobj);

    public native int ssudpGetLoginDate(int pobj);

    /*********************************/
    static private native int ssudpDiscoverInit();

    static private native int ssudpDiscoverFree(int handle);

    static private native int ssudpDiscoverTimeout(int handle, int timeout);

    static private native int ssudpDiscoverScan(int handle, int port, int mask, int ip);

    static private native int ssudpDiscoverResult(int handle, byte[] strcid, byte[] descriptor, byte[] deviceType);

    /*********************************/

    Packet packet = null;
    Stream stream = null;
    ClientConfig config = null;
    int clientId = 0;
    int pcsLink = 0;
    boolean isConnected = false;
    boolean isExit = false;
    boolean isIdle = false;
    Context context;

    public SSUDPClient(Context ctx, String cid, String pwd) {
        this.context = ctx;
        String strUuid = createConnectUUID();
        config = new ClientConfig(cid, pwd, strUuid);
        packet = new Packet();
        stream = new Stream();
        //ssudpHelp();
    }

    abstract boolean connect();

    abstract void disconnect();

    abstract void closeClient();

    abstract int send(byte[] buffer, int len, int flags);

    void initNetMask() {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo di = wm.getDhcpInfo();
        config.self_addr_ip = di.ipAddress;

        clientId = ssudpNewClient(config);

        if (di.netmask == 0) {
            di.netmask = di.gateway | 0xFF000000;
        }
        ssudpSetRoute(clientId, di.ipAddress, di.gateway, di.netmask);
    }

    int sendUUID() {
        byte[] uuid = new byte[20];
        uuid[0] = SSUDPConst.TAGID_CONNECT_UUID;
        uuid[1] = 16;
        uuid[2] = 0;
        uuid[3] = 0;

        System.arraycopy(config.struuid.getBytes(), 0, uuid, 4, 16);

        return send(uuid, 20, SSUDPConst.SS_WAIT);
    }

    void delayMS(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String createConnectUUID() {
        byte[] uuid = new byte[16];
        (new Random()).nextBytes(uuid);
        return String.format("%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
                uuid[0], uuid[1], uuid[2], uuid[3], uuid[4], uuid[5], uuid[6], uuid[7],
                uuid[8], uuid[9], uuid[10], uuid[11], uuid[12], uuid[13], uuid[14], uuid[15]);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isIdle() {
        return isIdle;
    }

}
