package com.eli.oneos.model.oneos.transfer;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public enum TransferException {
    NONE,
    UNKNOWN_EXCEPTION,
    LOCAL_SPACE_INSUFFICIENT,
    SERVER_SPACE_INSUFFICIENT,
    FAILED_REQUEST_SERVER,
    ENCODING_EXCEPTION,
    IO_EXCEPTION,
    FILE_NOT_FOUND,
    SERVER_FILE_NOT_FOUND,
    SOCKET_TIMEOUT,
    WIFI_UNAVAILABLE,
    SSUDP_DISCONNECTED
}
