package www.glinkwin.com.glink.ssudp;

/**
 * Created by gaoyun@eli-tech.com on 2016/6/24.
 */
public class SSUDPFileChunk {
    public String session;
    public String path;
    public long chunks;
    public long chunk;
    public boolean result;
    public int errorNo;
    public byte[] body;
    public int length;

    public SSUDPFileChunk(String session, String path, long chunks, long chunk) {
        this.session = session;
        this.path = path;
        this.chunks = chunks;
        this.chunk = chunk;
    }

    /**
     * init body bytes
     *
     * @param length
     */
    public void body(int length) {
        this.body = new byte[length];
        this.length = length;
    }

    public void reset() {
        result = true;
        errorNo = 0;
        body = null;
        length = 0;
    }
}
