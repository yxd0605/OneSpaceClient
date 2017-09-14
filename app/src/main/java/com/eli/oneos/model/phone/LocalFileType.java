package com.eli.oneos.model.phone;

import com.eli.oneos.R;

public enum LocalFileType {
    /**
     * 文件目录
     */
    PRIVATE,
    /**
     * 下载目录
     */
    DOWNLOAD,
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
     * 安装包
     */
    APP,
    /**
     * 压缩包
     */
    ZIP;

    public static int getTypeName(LocalFileType type) {
        int name = R.string.file_type_private;
        if (type == DOWNLOAD) {
            name = R.string.file_type_download;
        } else if (type == DOC) {
            name = R.string.file_type_doc;
        } else if (type == VIDEO) {
            name = R.string.file_type_video;
        } else if (type == AUDIO) {
            name = R.string.file_type_audio;
        } else if (type == PICTURE) {
            name = R.string.file_type_pic;
        } else if (type == APP) {
            name = R.string.file_type_app;
        }

        return name;
    }
}
