package www.glinkwin.com.glink.ssudp;

/**
 * Created by Administrator on 2015/12/17.
 */
public class ClientConfig {
    private static int IDENTITY = 0;

    private int id = 0;
    public String struuid;
    public String strcid;
    public String strpwd;
    public String strifname = "en0";


    public int tx_segment_size_max = 1024;
    public int rx_udp_channel_max = 4;
    public int tx_udp_channel_max = 4;

    public int rx_cache_size = 1024 * 1024 * 5;
    public int tx_cache_size = 1024 * 1024 * 5;

    public int tx_pack_size_max = 4096;
    public int rx_pack_size_max = 4096;

    public int tx_delay_us = 0;

    public int self_addr_port = 0;
    public int self_addr_ip = 0;

    /************************************/
    //链接后下面数据将被改变
    //public int gObj;
    //public int pcsLink;
    public ClientConfig(String strcid, String strpwd, String struuid) {
        this.strcid = strcid;
        this.strpwd = strpwd;
        this.struuid = struuid;
        this.id = IDENTITY++;
    }

    /**
     * for logger
     *
     * @return
     */
    public String id() {
        return "Client" + struuid + ": ";
    }
}

