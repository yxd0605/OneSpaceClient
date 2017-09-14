package www.glinkwin.com.glink.ssudp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by gaoyun@eli-tech.com on 2016/6/24.
 */
public class SSUDPUpload extends SSUDPClient {
    private static final String TAG = SSUDPUpload.class.getSimpleName();
    private static final int TIME_OUT = 10 * 1000;

    public SSUDPUpload(Context ctx, String cid, String pwd) {
        super(ctx, cid, pwd);
    }

    @Override
    boolean connect() {
        initNetMask();

        if (clientId == 0) {
            setConnect(false);
        } else {
            pcsLink = ssudpConnect(clientId);

            if (pcsLink != 0) {
                delayMS(100);
                sendUUID();
                isIdle = true;
                setConnect(true);
            } else {
                setConnect(false);
            }
            Log.d(TAG, config.id() + "---- Connect result: " + isConnected);
        }

        return isConnected;
    }

    @Override
    public void disconnect() {
        if (pcsLink != 0) {
            ssudpDisconnect(pcsLink);
            delayMS(300);
            ssudpClose(pcsLink);
//            delayMS(600);
            pcsLink = 0;
        }
    }

    @Override
    public void closeClient() {
        if (clientId != 0) {
            ssudpCloseClient(clientId);
            clientId = 0;
        }
    }

    @Override
    public int send(byte[] buffer, int len, int flags) {
        len = ssudpSend(pcsLink, buffer, len, flags);
        if (len == -1) {
            setConnect(false);
            return SSUDPConst.SSUDP_ERROR_DISCONNECT;
        }

        return len;
    }

    public void destroy() {
        disconnect();
        closeClient();
    }

    private int receive(byte[] buffer, int offset, int lens, int flags) {
        if (pcsLink == 0) {
            return -1;
        }

        lens = ssudpRecv(pcsLink, buffer, offset, lens, flags);
        if (lens == -1) {
            setConnect(false);
        }

        return lens;
    }

    private int receive() {
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
            Log.d(TAG, config.id() + "Content Len: " + rdLen);
            Log.d(TAG, config.id() + "Content Type: " + stream.type);

            while (rdLen != 0) {
                ret = receive(stream.buffer, rdPos, rdLen, SSUDPConst.SS_WAIT);
//                Log.d(TAG, config.id() + "Received Len: " + ret);
                if (ret < 0) {
                    return -1;
                }

                rdLen -= ret;
                rdPos += ret;
            }
        }

        return stream.length;
    }

    public SSUDPFileChunk upload(SSUDPFileChunk fileChunk) {
        if (!isConnected()) {
            connect();
        }

        Log.e(TAG, "Upload SSUDP state: " + isConnected());
        if (pcsLink == 0) {
            fileChunk.result = false;
            fileChunk.errorNo = SSUDPConst.SSUDP_ERROR_NOT_READY;
            Log.e(TAG, "Upload SSUDP Not ready...");
            return fileChunk;
        }

        if (!isConnected()) {
            fileChunk.result = false;
            fileChunk.errorNo = SSUDPConst.SSUDP_ERROR_DISCONNECT;
            Log.e(TAG, "Upload SSUDP disconnected...");
            return fileChunk;
        }

        String request = String.format(SSUDPConst.FMT_TRANS_FILE, fileChunk.session, fileChunk.chunks, fileChunk.chunk, fileChunk.path);
        byte[] reqBytes = request.getBytes();
        int msgLen = reqBytes.length;
        int fileLen = fileChunk.length;
        byte buffer[] = new byte[4 + msgLen + fileLen + 1];
        SSUDPType.int2byte(SSUDPConst.TAGID_FTP_UPLOAD | ((msgLen + fileLen + 1) << 8), buffer, 0);
        System.arraycopy(reqBytes, 0, buffer, 4, msgLen);
        System.arraycopy(fileChunk.body, 0, buffer, 4 + msgLen, fileLen);

        int send = send(buffer, buffer.length, SSUDPConst.SS_WAIT);
        if (send < 0) {
            Log.e(TAG, config.id() + ">>>> Send failed, result=" + send + "; connected=" + isConnected());
            fileChunk.result = false;
            fileChunk.errorNo = SSUDPConst.SSUDP_ERROR_DISCONNECT;
            return fileChunk;
        } else {
            Log.d(TAG, config.id() + ">>>> Send success, len=" + send);
        }

        Log.e(TAG, config.id() + ">>>> Start receive upload result...");
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin <= TIME_OUT) {
            int len = receive();
            if (len <= 0) {
                Log.e(TAG, config.id() + ">>>> Receive none content...");
                fileChunk.result = false;
                fileChunk.errorNo = SSUDPConst.SSUDP_ERROR_NO_CONTENT;
            } else {
                if (stream.type == SSUDPConst.TAGID_FTP_UPLOAD_ACK) {
                    Log.d(TAG, ">>>> Receive response: " + new String(stream.buffer).trim().replaceAll("\r\n", "--"));
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.buffer);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    try {
                        long chunks = 0;
                        long chunk = 0;
                        String path = null;
                        boolean result = false;

                        String line;
                        int count = 0;
                        while ((line = bufferedReader.readLine()) != null && count < 5) {
                            count++;

                            if (line.startsWith("SESSION:")) {
                                continue;
                            }
                            if (line.startsWith("CHUNKS:")) {
                                chunks = Integer.parseInt(line.replaceFirst("CHUNKS:", ""));
                                continue;
                            }
                            if (line.startsWith("CHUNK:")) {
                                chunk = Integer.parseInt(line.replaceFirst("CHUNK:", ""));
                                continue;
                            }
                            if (line.startsWith("PATH:")) {
                                path = line.replaceFirst("PATH:", "");
                                continue;
                            }
                            if (line.startsWith("RESULT:")) {
                                result = Boolean.parseBoolean(line.replaceFirst("RESULT:", ""));
                                continue;
                            }
                        }
                        bufferedReader.close();
                        if (result && (fileChunk.chunk == chunk) && (chunks == fileChunk.chunks) && path.equals(fileChunk.path)) {
                            fileChunk.result = true;
                        } else {
                            Log.d(TAG, ">>>> Upload failed !!");
                            fileChunk.result = false;
                            fileChunk.errorNo = SSUDPConst.SSUDP_ERROR_EXCEPTION;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        fileChunk.result = false;
                        fileChunk.errorNo = SSUDPConst.SSUDP_ERROR_EXCEPTION;
                    }

                    break;
                }
            }
        }
        Log.d(TAG, config.id() + ">>>> Stop receive file...");

        return fileChunk;
    }

    private void setConnect(boolean connect) {
        isConnected = connect;
    }
}
