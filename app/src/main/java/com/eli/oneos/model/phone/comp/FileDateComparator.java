package com.eli.oneos.model.phone.comp;

import com.eli.oneos.model.phone.LocalFile;

import java.util.Comparator;

/**
 * Comparator for File Date
 */
public class FileDateComparator implements Comparator<LocalFile> {

    @Override
    public int compare(LocalFile lf1, LocalFile lf2) {
        if (lf1 == lf2) {
            return 0;
        }

        if (lf1.getDate() < lf2.getDate()) {
            return 1;
        } else if (lf1.getDate() > lf2.getDate()) {
            return -1;
        } else {
            return 0;
        }
    }
}
