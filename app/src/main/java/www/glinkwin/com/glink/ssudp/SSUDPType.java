package www.glinkwin.com.glink.ssudp;


/***************************************************************
 * Copyright (C) 2015 盛耀微电子科技有限公司
 * www.glinkwin.com
 * Author :wxj@glinkwin.com
 * Version:1.0
 * Date:2015-11-10
 * History:
 ***************************************************************/

public class SSUDPType {

    public static short byte2short(byte[] bytes, int offset) {
        short var = 0;
        var |= ubyteToInt(bytes[1 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[0 + offset]);
        return var;
    }

    public static void short2byte(short v, byte[] bytes, int offset) {
        bytes[offset++] = (byte) (v & 0x00ff);
        bytes[offset++] = (byte) ((v >> 8) & 0x00ff);
    }

    public static void int2byte(int v, byte[] bytes, int offset) {
        bytes[offset++] = (byte) (v & 0xff);
        bytes[offset++] = (byte) ((v >> 8) & 0xff);
        bytes[offset++] = (byte) ((v >> 16) & 0xff);
        bytes[offset++] = (byte) ((v >> 24) & 0xff);
    }

    public static long byte2uint2long(byte[] bytes, int offset) {
        long var = 0;
        var |= ubyteToInt(bytes[3 + offset]);// -1
        var <<= 8;
        var |= ubyteToInt(bytes[2 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[1 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[0 + offset]);
        return var;
    }

    public static int byte2int(byte[] bytes, int offset) {
        int var = 0;
        var |= ubyteToInt(bytes[3 + offset]);// -1
        var <<= 8;
        var |= ubyteToInt(bytes[2 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[1 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[0 + offset]);
        return var;
    }

    public static long uint2ulong(int v) {

        long var = v & 0x7fffffff;
        if (v < 0) var += 0x0080000000l;
        return var;
    }

    public static long byte2long(byte[] bytes, int offset) {
        long var = 0;
        var |= ubyteToInt(bytes[7 + offset]);// -1
        var <<= 8;
        var |= ubyteToInt(bytes[6 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[5 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[4 + offset]);

        var |= ubyteToInt(bytes[3 + offset]);// -1
        var <<= 8;
        var |= ubyteToInt(bytes[2 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[1 + offset]);
        var <<= 8;
        var |= ubyteToInt(bytes[0 + offset]);

        return var;
    }

    public static int ubyteToInt(byte b) {
        int r = b & 0x7f;
        if (b < 0) r += 0x80;
        return r;
    }
}
