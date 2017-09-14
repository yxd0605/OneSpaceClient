package com.eli.oneos.model.phone;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.model.phone.comp.FileDateComparator;
import com.eli.oneos.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LocalSortTask extends AsyncTask<Integer, Integer, String[]> {
    private static final String TAG = LocalSortTask.class.getSimpleName();

    private LinkedList<String> mExtensionList = null;
    private Activity activity;
    private LocalFileType type = LocalFileType.PICTURE;
    private String filter = null;
    private List<LocalFile> mFileList = new ArrayList<>();
    private List<String> mSectionList = new ArrayList<>();
    private onLocalSortListener mListener;
    private String fmtDate;

    public LocalSortTask(Activity activity, LocalFileType type, String filter, onLocalSortListener mListener) {
        this.activity = activity;
        this.type = type;
        this.filter = filter;
        this.mListener = mListener;
        fmtDate = activity.getResources().getString(R.string.fmt_time_line);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onStart(type);
        mFileList.clear();
        mSectionList.clear();
    }

    @Override
    protected String[] doInBackground(Integer... params) {
        try {
            getExtension(type);
            getSortList(type);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.onComplete(type, mFileList, mSectionList);
        }
    }

    private void getSortList(LocalFileType type) throws IllegalArgumentException {
        if (activity == null) {
            Log.e(TAG, "getSortList getActivity is null");
            return;
        }
        ContentResolver mResolver = activity.getApplicationContext().getContentResolver();
        int column_index = 0;
        Cursor cursor = null;
        boolean isMedia = true;
        if (type == LocalFileType.AUDIO) {
            String[] projections = {MediaStore.Audio.Media.DATA};
            cursor = mResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projections, null, null, null);
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            }
        } else if (type == LocalFileType.VIDEO) {
            String[] projections = {MediaStore.Video.Media.DATA};
            cursor = mResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projections, null, null, null);
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            }
        } else if (type == LocalFileType.PICTURE) {
            String[] projections = {MediaStore.Images.Media.DATA};
            cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projections, null, null, null);
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            }
        } else {
            String[] projections = {MediaStore.Files.FileColumns.DATA};
            Uri uri = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
            cursor = mResolver.query(uri, projections, selection, null, null);
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            }
            isMedia = false;
        }

        if (cursor == null) {
            return;
        } else {
            cursor.moveToFirst();
            /** get the video/audio/image files */
            while (cursor.moveToNext() && isMedia) {
                String path = cursor.getString(column_index);
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    if (filter != null && !file.getName().contains(filter)) {
                        continue;
                    }

                    mFileList.add(new LocalFile(file));
                }
            }
            /** get other type files */
            if (!isMedia && cursor.moveToNext()) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(column_index);
                    if (path.indexOf(".") > -1) {
                        String extension = path.substring(path.lastIndexOf("."), path.length()).toUpperCase();
                        if (null == mExtensionList || mExtensionList.contains(extension)) {
                            File file = new File(path);
                            if (file.exists() && file.isFile()) {
                                if (filter != null && !file.getName().contains(filter)) {
                                    continue;
                                }

                                mFileList.add(new LocalFile(file));
                            }
                        }
                    }
                }
            }
            cursor.close(); // needs to close cursor

            if (type == LocalFileType.PICTURE) {
                for (LocalFile f : mFileList) {
                    File file = f.getFile();
                    long date = getPhotoDate(file);
                    f.setDate(date);
                }
            } else {
                for (LocalFile f : mFileList) {
                    File file = f.getFile();
                    f.setDate(file.lastModified());
                }
            }

            if (type != LocalFileType.PRIVATE && type != LocalFileType.DOWNLOAD) {
                Collections.sort(mFileList, new FileDateComparator());
                String tmpDate = "";
                int section = -1;
                for (int i = 0; i < mFileList.size(); i++) {
                    LocalFile file = mFileList.get(i);
                    String date = FileUtils.fmtTimeByZone(file.getDate() / 1000, fmtDate);
                    if (!tmpDate.equals(date)) {
                        tmpDate = date;
                        section++;
                        mSectionList.add(date);
                    }
                    file.setSection(section);
                }
            }
        }
    }

    private LinkedList<String> getExtension(LocalFileType type) {
        if (type == LocalFileType.PRIVATE || type == LocalFileType.DOWNLOAD) {
            return null;
        }

        mExtensionList = new LinkedList<>();
        if (type == LocalFileType.AUDIO) {
            mExtensionList.add(".MP3");
            mExtensionList.add(".WMA");
            mExtensionList.add(".WAV");
            mExtensionList.add(".AAC");
            mExtensionList.add(".APE");
        } else if (type == LocalFileType.VIDEO) {
            mExtensionList.add(".AVI");
            mExtensionList.add(".ASF");
            mExtensionList.add(".WMV");
            mExtensionList.add(".3GP");
            mExtensionList.add(".FLV");
            mExtensionList.add(".RMVB");
            mExtensionList.add(".RM");
            mExtensionList.add(".MP4");
        } else if (type == LocalFileType.PICTURE) {
            mExtensionList.add(".BMP");
            mExtensionList.add(".JPEG");
            mExtensionList.add(".JPG");
            mExtensionList.add(".PNG");
            mExtensionList.add(".GIF");
        } else if (type == LocalFileType.DOC) {
            mExtensionList.add(".DOC");
            mExtensionList.add(".XLS");
            mExtensionList.add(".TXT");
            mExtensionList.add(".PPT");
            mExtensionList.add(".PDF");
        } else if (type == LocalFileType.ZIP) {
            mExtensionList.add(".RAR");
            mExtensionList.add(".TAR");
            mExtensionList.add(".ISO");
            mExtensionList.add(".JAR");
            mExtensionList.add(".ZIP");
        } else if (type == LocalFileType.APP) {
            mExtensionList.add(".APK");
        }

        return mExtensionList;
    }

    /**
     * get photo date
     *
     * @param file
     * @return photo date
     */
    private long getPhotoDate(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            if (exif != null) {
                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if (dateTime != null) {
                    String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
                    return FileUtils.parseFmtTime(date, "yyyy:MM:dd HH:mm:ss");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Get Photo Date Exception", e);
        }

        return 0;
    }

    public interface onLocalSortListener {
        void onStart(LocalFileType type);

        void onComplete(LocalFileType type, List<LocalFile> fileList, List<String> sectionList);
    }
}