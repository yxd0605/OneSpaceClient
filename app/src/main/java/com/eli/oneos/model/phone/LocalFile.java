package com.eli.oneos.model.phone;

import java.io.File;
import java.io.Serializable;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class LocalFile implements Serializable {
    private static final long serialVersionUID = 111181567L;

    private File file;
    // for sticky header
    private int section = 0;
    // photo date time
    private long date = 0;
    // weather is download directory
    private boolean isDownloadDir = false;
    // weather is backup directory
    private boolean isBackupDir = false;

    public LocalFile(File file) {
        this.file = file;
        this.section = 0;
    }

    public LocalFile(File file, int section) {
        this.file = file;
        this.section = section;
    }

    public long getTime() {
        return file.lastModified();
    }

    public String getName() {
        return file.getName();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public long lastModified() {
        return file.lastModified();
    }

    public long length() {
        return file.length();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isDownloadDir() {
        return isDownloadDir;
    }

    public void setIsDownloadDir(boolean isDownload) {
        this.isDownloadDir = isDownload;
    }

    public boolean isBackupDir() {
        return isBackupDir;
    }

    public void setIsBackupDir(boolean isBackup) {
        this.isBackupDir = isBackup;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof LocalFile) {
            LocalFile file = (LocalFile) other;
            return this.file.equals(file);
        }

        return false;
    }
}
