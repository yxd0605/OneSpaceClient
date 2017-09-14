package www.glinkwin.com.glink.ssudp;

public class SSUDPConst {
    /**
     * 客户端最大Client数
     */
    public static final int MAX_CLIENTS_COUNT = 1;

    public static final String BROADCAST_ACTION_SSUDP = "com.eli.onespace.ssudp.STATE_CHANGED";
    public static final String EXTRA_SSUDP_STATE = "SSUDPState";

    public static final String FMT_TRANS_FILE = "SESSION:%s\r\nCHUNKS:%s\r\nCHUNK:%s\r\nPATH:%s\r\n";

    /*数据加控制命令都用ssudp安全传输*/
    public static final byte TAGID_UNKNOW = 0x00;
    public static final byte TAGID_CONNECT_UUID = 0x01;
    public static final byte TAGID_CONNECT_UUIDACK = 0x02;


    public static final byte TAGID_FTP_REQUEST = 0x29;
    public static final byte TAGID_FTP_REQUEST_ACK = 0x2A;
    public static final byte TAGID_FTP_DOWNLOAD = 0x2B;
    public static final byte TAGID_FTP_DOWNLOAD_ACK = 0x2C;
    public static final byte TAGID_FTP_UPLOAD = 0x2D;
    public static final byte TAGID_FTP_UPLOAD_ACK = 0x2E;


    public static final int SS_WAIT = 0;
    public static final int SS_NOWAIT = 1;


    //enum SSUDP_EVENT_ID
    public static final int SSUDP_EVENT_HOST_OFFLINE = 0;
    public static final int SSUDP_EVENT_LINK_STS_CHANGE = 1;
    public static final int SSUDP_EVENT_ACCETP_CS = 2;
    public static final int SSUDP_EVENT_REMOTE_CS_CLOSE = 3;
    public static final int SSUDP_EVENT_CS_CLOSE = 4;
    public static final int SSUDP_EVENT_SSUDP_VER_ERROR = 5;
    public static final int SSUDP_EVENT_CONNECT_FULL = 6;
    public static final int SSUDP_EVENT_CONNECT_ERROR = 7;

    //
    public static final int LKSTS_IDLE = 0;
    public static final int LKSTS_REGISTER = 1;
    public static final int LKSTS_LINKING = 2;
    public static final int LKSTS_P2P_LINKOK = 7;
    public static final int LKSTS_P2P_IDFING = 8;
    public static final int LKSTS_P2P_IDFOK = 9;
    public static final int LKSTS_VIDEO_NOW = 10;
    public static final int LKSTS_VIDEO_CLOUD = 11;


    public static final int SSUDP_MSG_STREAM = 0x100;
    public static final int SSUDP_MSG_EVENT = 0x101;
    public static final int SSUDP_MSG_CHANNEL = 0x102;
    public static final int SSUDP_MSG_STATE = 0x103;

    //----------------- SSUDP error info --------------------
    public static final int SSUDP_ERROR_NOT_READY = -0x10001;
    public static final int SSUDP_ERROR_DISCONNECT = -0x10002;
    public static final int SSUDP_ERROR_IS_BUSY = -0x10003;
    public static final int SSUDP_ERROR_NO_CONTENT = -0x10004;
    public static final int SSUDP_ERROR_EXCEPTION = -0x10004;
}


