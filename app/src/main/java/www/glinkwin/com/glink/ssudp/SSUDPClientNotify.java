package www.glinkwin.com.glink.ssudp;

/**
 * Created by wxj on 16/4/3.
 */
public class SSUDPClientNotify {
    /**
    struct NOTIFY{
        u32 cmd;
        u32 msgid;
        // u8 param[8];
    };

    struct NOTIFY_MSG{
        struct STREAM_HD msg;
        struct NOTIFY notify;
    };
     **/
    public byte tag;
    public int lens;//3
    public int cmd;
    public int msgid;
    public int msgOffset;
    public byte []buffer = new byte[512];
    public int msgLens;


    public void byte2member(byte [] streambuf,int lens){
        System.arraycopy(streambuf,0,buffer,0,lens);
        tag = buffer[0];
        lens = SSUDPType.byte2int(buffer, 0);
        lens >>= 8;
        cmd     =  SSUDPType.byte2int(buffer, 4);
        msgid   =  SSUDPType.byte2int(buffer, 4 + 4);

        msgOffset = 4+4+4;

        msgLens = lens-8;

    }

}
