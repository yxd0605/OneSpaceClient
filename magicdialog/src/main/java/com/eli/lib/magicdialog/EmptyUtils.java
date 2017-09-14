package com.eli.lib.magicdialog;

import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class EmptyUtils {

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isEmpty(String s) {
        if (null == s) {
            return true;
        }

        if (s.trim().length() == 0) {
            return true;
        }

        return false;
    }

}
