package com.eli.oneos.model.oneos;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;

import static com.eli.oneos.model.FileManageAction.SHARE;

public enum OneOSFileType {
    /**
     * 文件目录
     */
    PRIVATE,
    /**
     * 公共目录
     */
    PUBLIC,
    /**
     * 回收站
     */
    RECYCLE,
    /**
     * 文档
     */
    DOC,
    /**
     * 图片
     */
    PICTURE,
    /**
     * 视频
     */
    VIDEO,
    /**
     * 音频
     */
    AUDIO,
    /**
     * 共享
    */
    SHARE;

    public static String getServerTypeName(OneOSFileType type) {
        if (type == DOC) {
            return "doc";
        } else if (type == VIDEO) {
            return "video";
        } else if (type == AUDIO) {
            return "audio";
        } else {
            return "pic";
        }
    }

    public static int getTypeName(OneOSFileType type) {
        int name = R.string.file_type_private;
        if (type == PUBLIC) {
            name = R.string.file_type_public;
        } else if (type == RECYCLE) {
            name = R.string.file_type_cycle;
        } else if (type == DOC) {
            name = R.string.file_type_doc;
        } else if (type == VIDEO) {
            name = R.string.file_type_video;
        } else if (type == AUDIO) {
            name = R.string.file_type_audio;
        } else if (type == PICTURE) {
            name = R.string.file_type_pic;
        } else if (type == SHARE){
            name = R.string.file_type_share;
        }

        return name;
    }

    public static String getRootPath(OneOSFileType type) {
        String path = null;
        if (type == OneOSFileType.PRIVATE) {
            path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        } else if (type == OneOSFileType.PUBLIC) {
            path = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
        } else if (type == OneOSFileType.RECYCLE) {
            path = OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR;
        } else if (type == OneOSFileType.SHARE){
            path = OneOSAPIs.ONE_OS_SHARE_ROOT_DIR;
        }

        return path;
    }
}
