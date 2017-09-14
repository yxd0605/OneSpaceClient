package com.eli.oneos.model.oneos.comp;

import com.eli.oneos.model.oneos.OneOSFile;

import java.util.Comparator;

/**
 * Comparator for File Name
 */
public class OneOSFileTimeComparator implements Comparator<OneOSFile> {

    @Override
    public int compare(OneOSFile file1, OneOSFile file2) {
        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        }

        if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        if (file1.getTime() < file2.getTime()) {
            return 1;
        } else if (file1.getTime() > file2.getTime()) {
            return -1;
        } else {
            return 0;
        }
    }
}