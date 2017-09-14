package com.eli.oneos.model;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/15.
 */
public enum FileViewerType {
    LIST,
    GRID;

    public static boolean isList(FileViewerType type) {
        return type == FileViewerType.LIST;
    }

    public static boolean isList(int id) {
        return id == 0;
    }
}
