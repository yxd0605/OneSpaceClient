package com.eli.oneos.model.oneos.transfer;

import java.io.File;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public class UploadElement extends TransferElement {
    private File file;
    // needs check if file exist
    private boolean check = false;
    // overwrite if target file exist
    private boolean overwrite = false;

    public UploadElement() {
    }

    public UploadElement(File file, String uploadPath) {
        this.file = file;
        this.toPath = uploadPath;
    }

    public UploadElement(File file, String uploadPath, boolean check) {
        this.file = file;
        this.toPath = uploadPath;
        this.check = check;
    }

    public boolean isUploadToPrivateDir() {
        return toPath.startsWith("/");
    }

    /**
     * Whether is download file
     *
     * @return true or false
     */
    @Override
    protected boolean isDownload() {
        return false;
    }

    /**
     * Get transmission source file path
     */
    @Override
    public String getSrcPath() {
        return file.getPath();
    }

    /**
     * Get transmission source file name
     */
    @Override
    public String getSrcName() {
        return file.getName();
    }

    /**
     * Get transmission source file size
     */
    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public String toString() {
        return "{src:" + file.getAbsolutePath() + ", target:" + toPath + ", overwrite:" + overwrite + "}";
    }

    // ===============getter and setter method======================
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    // ===============getter and setter method======================
}
