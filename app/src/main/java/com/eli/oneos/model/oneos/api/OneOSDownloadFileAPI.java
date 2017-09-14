package com.eli.oneos.model.oneos.api;

import android.content.Context;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.OnTransferFileListener;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.SDCardUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import www.glinkwin.com.glink.ssudp.SSUDPFileChunk;
import www.glinkwin.com.glink.ssudp.SSUDPManager;

/**
 * OneSpace OS Download File API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/14.
 */
public class OneOSDownloadFileAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSDownloadFileAPI.class.getSimpleName();
    private static final int HTTP_BUFFER_SIZE = 1024 * 16;
    private static final double SSUDP_CHUNK_SIZE = 1024 * 1024;

    private OnTransferFileListener<DownloadElement> listener;
    private DownloadElement downloadElement;
    private boolean isInterrupt = false;
    private LoginSession loginSession;

    public OneOSDownloadFileAPI(LoginSession loginSession, DownloadElement element) {
        super(loginSession);
        this.loginSession = loginSession;
        this.downloadElement = element;
    }

    public void setOnDownloadFileListener(OnTransferFileListener<DownloadElement> listener) {
        this.listener = listener;
    }

    public boolean download() {
        if (null != listener) {
            listener.onStart(url, downloadElement);
        }

        if (LoginManage.getInstance().isHttp()) {
            doHttpDownload();
        } else {
            doSSUDPDownload();
        }

        if (null != listener) {
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "download over");
            listener.onComplete(url, downloadElement);
        }

        return downloadElement.getState() == TransferState.COMPLETE;
    }

    public void stopDownload() {
        isInterrupt = true;
        Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "Upload Stopped");
    }

    private void doHttpDownload() {
        url = OneOSAPIs.genDownloadUrl(loginSession, downloadElement.getFile());

        // set element download state to start
        downloadElement.setState(TransferState.START);
        isInterrupt = false;
        String session = loginSession.getSession();
        try {
            HttpGet httpGet = new HttpGet(url);
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Download file: " + url);
            if (downloadElement.getOffset() < 0) {
                Logger.p(LogLevel.WARN, Logged.DOWNLOAD, TAG, "error position, position must greater than or equal zero");
                downloadElement.setOffset(0);
            }
            httpGet.setHeader("Cookie", "session=" + session);

            if (downloadElement.getOffset() > 0) {
                httpGet.setHeader("Range", "bytes=" + String.valueOf(downloadElement.getOffset()) + "-");
            }
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code != 200 && code != 206) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: status code=" + code);
                downloadElement.setState(TransferState.FAILED);
                if (code == 404) {
                    downloadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                } else {
                    downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                }
                return;
            }

            long fileLength = entity.getContentLength();
            if (fileLength < 0) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: content length=" + fileLength);
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            } else if (downloadElement.isCheck() && fileLength > SDCardUtils.getDeviceAvailableSize(downloadElement.getToPath())) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "SD Available Size Insufficient");
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                return;
            }
//                fileLength += downloadElement.getOffset();
//                downloadElement.setTotalFileLength(fileLength);

            saveData(entity.getContent(), httpClient);

        } catch (HttpHostConnectException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            e.printStackTrace();
        }
    }

    private void httpPostDownload() {
        Context context = MyApplication.getAppContext();
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        String session = loginSession.getSession();
        String url = downloadElement.getUrl();
        String srcPath = downloadElement.getSrcPath();

        if (session == null) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Session is null");
            return;
        }

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("session", session));
        param.add(new BasicNameValuePair("srcPath", srcPath));

        try {
            HttpPost httpRequest = new HttpPost(url);
            if (downloadElement.getOffset() > 0) {
                String tmpPath = downloadElement.getToPath() + File.separator + downloadElement.getTmpName();
                File tmpFile = new File(tmpPath);
                if (!tmpFile.exists()) {
                    Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Temporary file is missing, resetBackupAlbum download offset position");
                    downloadElement.setOffset(0);
                } else {
                    long tmpLen = tmpFile.length();
                    if (tmpLen != downloadElement.getOffset()) {
                        Logger.p(LogLevel.WARN, Logged.DOWNLOAD, TAG, "Temporary file length not equals offset position, reset download offset position and delete temporary file");
                        downloadElement.setOffset(0);
                        tmpFile.delete();
                    }
                }

                Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Download offset position: " + downloadElement.getOffset());
                httpRequest.setHeader("Range", "bytes=" + String.valueOf(downloadElement.getOffset()) + "-");
            } else if (downloadElement.getOffset() < 0) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Error offset position: " + downloadElement.getOffset() + ", reset position to 0");
                downloadElement.setOffset(0);
            }

            HttpClient httpClient = new DefaultHttpClient();
            // httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT,
            // 5000);
            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            httpRequest.setEntity(new UrlEncodedFormEntity(param, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            HttpEntity entity = httpResponse.getEntity();
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code != 200 && code != 206) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: status code=" + code);
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            }
            long fileLength = entity.getContentLength();
            // Logged.d(LOG_TAG, "download file length = " + fileLength);
            if (fileLength < 0) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "ERROR: content length=" + fileLength);
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            } else if (fileLength > SDCardUtils.getDeviceAvailableSize(downloadElement.getToPath())) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "SDCard Available Size Insufficient");
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                return;
            }
//            Header header = httpResponse.getFirstHeader("Content-Ranges");
//            if (header != null) {
//                String contentRanges = header.getValue();
//                int last = contentRanges.lastIndexOf('/');
//                String totalString = contentRanges.substring(last + 1, contentRanges.length());
//                fileLength = Long.valueOf(totalString);
//                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "header name=" + header.getName() + ", value=" + header.getValue());
//            }
//            downloadElement.getFile().setSize(fileLength);

            // set element download state to start
            downloadElement.setState(TransferState.START);
            saveData(entity.getContent(), httpClient);

        } catch (HttpHostConnectException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        }
    }

    private void completeDownload(String tmpPath, long downloadLen) {
        if (isInterrupt) {
            downloadElement.setState(TransferState.PAUSE);
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Download interrupt");
        } else {
            if (downloadElement.getSize() > 0 && downloadLen != downloadElement.getSize()) {
                Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, String.format("Download file length[%d] is not equals file real length[%d]", downloadLen, downloadElement.getSize()));
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            } else {
                File toFile = new File(downloadElement.getToPath() + File.separator + downloadElement.getSrcName());
                String toName = downloadElement.getSrcName();
                int addition = 1;
                while (toFile.exists()) {
                    String name = toFile.getName();
                    int index = name.indexOf(".");
                    if (index >= 0) {
                        String prefix = name.substring(0, index);
                        String suffix = name.substring(index, name.length());
                        toName = prefix + "_" + addition + suffix;
                    } else {
                        toName = name + "_" + addition;
                    }
                    toFile = new File(downloadElement.getToPath() + File.separator + toName);
                    addition++;
                }
                downloadElement.setToName(toName);
                File tmpFile = new File(tmpPath);
                tmpFile.renameTo(toFile);

                downloadElement.setState(TransferState.COMPLETE);
            }
        }
    }

    private void saveData(InputStream input, HttpClient httpClient) {
        RandomAccessFile outputFile = null;
        long downloadLen = downloadElement.getOffset();
        try {
            File dir = new File(downloadElement.getToPath());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String tmpPath = downloadElement.getToPath() + File.separator + downloadElement.getTmpName();
            outputFile = new RandomAccessFile(tmpPath, "rw");
            outputFile.seek(downloadElement.getOffset());
            byte[] buffer = new byte[HTTP_BUFFER_SIZE];
            int nRead;
            int callback = 0; // for download progress callback
            while (!isInterrupt) {
                nRead = input.read(buffer, 0, buffer.length);
                if (nRead < 0) {
                    break;
                }
                outputFile.write(buffer, 0, nRead);
                downloadLen += nRead;
                downloadElement.setLength(downloadLen);
                callback++;
                if (null != listener && callback == 32) {
                    // callback every 512KB
                    listener.onTransmission(url, downloadElement);
                    callback = 0;
                }
            }
            downloadElement.setOffset(downloadElement.getLength());

            completeDownload(tmpPath, downloadLen);

            httpClient.getConnectionManager().shutdown();
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "Shut down http connection");
        } catch (FileNotFoundException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FILE_NOT_FOUND);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (SocketException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (outputFile != null) {
                    outputFile.close();
                }
            } catch (IOException e) {
                Logger.p(LogLevel.ERROR, Logged.DOWNLOAD, TAG, "Input/Output Stream closed error");
                e.printStackTrace();
            }
        }
    }

    private void doSSUDPDownload() {
        Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, ">>>> Start SSUDP download...");

        SSUDPManager ssudpManager = SSUDPManager.getInstance();

        // set element download state to start
        downloadElement.setState(TransferState.START);
        isInterrupt = false;
        String session = loginSession.getSession();

        long offset = downloadElement.getOffset();
        long size = downloadElement.getSize();
        if (offset >= size) {
            Logger.p(LogLevel.DEBUG, Logged.DOWNLOAD, TAG, "download offset[" + offset + "], file size[" + size + "], complete");
            downloadElement.setState(TransferState.COMPLETE);
            return;
        }

        if (offset < 0) {
            Logger.p(LogLevel.WARN, Logged.DOWNLOAD, TAG, "error position, position must greater than or equal zero");
            downloadElement.setOffset(0);
            offset = 0;
        }

        long chunks = (long) Math.ceil((double) size / SSUDP_CHUNK_SIZE);
        long chunk = (long) Math.floor((double) offset / SSUDP_CHUNK_SIZE);
        try {
            RandomAccessFile outputFile;
            File dir = new File(downloadElement.getToPath());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String tmpPath = downloadElement.getToPath() + File.separator + downloadElement.getTmpName();
            outputFile = new RandomAccessFile(tmpPath, "rw");
            outputFile.seek(downloadElement.getOffset());

//            int callback = 0;
            int retry = 0;
            long downloadLen = downloadElement.getOffset();
            SSUDPFileChunk fileChunk = new SSUDPFileChunk(session, downloadElement.getSrcPath(), chunks, chunk);
            while (!isInterrupt && fileChunk.chunk < fileChunk.chunks) {
                fileChunk = ssudpManager.ssudpDownloadRequest(fileChunk);
                if (fileChunk.result) {
                    outputFile.write(fileChunk.body);
                    fileChunk.chunk++;
//                    callback++;
                    downloadLen += fileChunk.length;
                    downloadElement.setLength(downloadLen);
                    if (null != listener/* && callback == 32*/) {
                        // callback every 512KB
                        listener.onTransmission(url, downloadElement);
//                        callback = 0;
                    }
                } else {
                    retry++;
                    if (retry > 5) {
                        downloadElement.setState(TransferState.FAILED);
                        downloadElement.setException(TransferException.SSUDP_DISCONNECTED);
                        break;
                    }
                }
            }
            downloadElement.setOffset(downloadElement.getLength());

            completeDownload(tmpPath, downloadLen);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
