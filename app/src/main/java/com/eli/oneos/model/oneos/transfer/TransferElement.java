package com.eli.oneos.model.oneos.transfer;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public abstract class TransferElement {

    protected long id;
    /**
     * Transmission url
     */
    protected String url;
    /**
     * Transmission source file path
     */
    protected String srcPath;
    /**
     * Transmission file target path
     */
    protected String toPath;
    /**
     * Source file size
     */
    protected long size;
    /**
     * Seek offset
     */
    protected long offset;
    /**
     * Transmitted length
     */
    protected long length;
    /**
     * Transmission end time
     */
    protected long time;
    /**
     * Transmission state
     */
    protected TransferState state = TransferState.WAIT;
    /**
     * Transmission exception
     */
    protected TransferException exception = TransferException.NONE;

    /**
     * Whether is download file
     *
     * @return true or false
     */
    protected abstract boolean isDownload();

    /**
     * Get transmission source file path
     */
    public abstract String getSrcPath();

    /**
     * Get transmission source file name
     */
    public abstract String getSrcName();

    /**
     * Get transmission source file size
     */
    public abstract long getSize();

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode ^= (url == null ? 0 : url.hashCode());
        hashCode ^= (srcPath == null ? 0 : srcPath.hashCode());

        return hashCode;
    }

    // ===============getter and setter method======================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    /**
     * Transmitted length
     */
    public long getLength() {
        return length;
    }

    /**
     * Transmitted length
     */
    public void setLength(long length) {
        this.length = length;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public TransferState getState() {
        return state;
    }

    public void setState(TransferState state) {
        this.state = state;
    }

    public TransferException getException() {
        return exception;
    }

    public void setException(TransferException exception) {
        this.exception = exception;
    }
    // ===============getter and setter method======================
}
