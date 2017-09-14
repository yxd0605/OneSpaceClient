package com.eli.oneos.ui.nav.progress;

public interface ProgressListener {

    void progress(long bytesRead, long contentLength, boolean done);

}
