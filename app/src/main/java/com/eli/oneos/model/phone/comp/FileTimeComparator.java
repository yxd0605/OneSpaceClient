package com.eli.oneos.model.phone.comp;

import com.eli.oneos.model.phone.LocalFile;

import java.io.File;
import java.util.Comparator;

/** Comparator for File LastModified */
public class FileTimeComparator implements Comparator<LocalFile> {

	@Override
	public int compare(LocalFile lf1, LocalFile lf2) {
		File file1 = lf1.getFile();
		File file2 = lf2.getFile();
		if (file1 == null || file2 == null) {
			return 0;
		}

		if (file1.isFile() && file2.isDirectory()) {
			return 1;
		} else if (file1.isDirectory() && file2.isFile()) {
			return -1;
		} else {
			if (file1.lastModified() < file2.lastModified()) {
				return 1;
			} else if (file1.lastModified() > file2.lastModified()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
