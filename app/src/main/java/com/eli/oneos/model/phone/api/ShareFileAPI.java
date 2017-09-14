package com.eli.oneos.model.phone.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.utils.MIMETypeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class ShareFileAPI {


    public boolean share(List<LocalFile> files, Context context) {
        boolean multiple = files.size() > 1;
        ArrayList<Uri> uris = new ArrayList<>();
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE : android.content.Intent.ACTION_SEND);
        if (!multiple) {
            String mimeType = MIMETypeUtils.getMIMEType(files.get(0).getName());
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(files.get(0).getFile()));
        } else {
            for (int i = 0; i < files.size(); i++) {
                Uri uri = Uri.fromFile(files.get(i).getFile());
                uris.add(uri);
            }
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, uris);
        }

        context.startActivity(intent);

        return true;
    }
}
