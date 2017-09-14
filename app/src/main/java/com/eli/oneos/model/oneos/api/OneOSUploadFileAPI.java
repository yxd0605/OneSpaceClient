package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.constant.OneSpaceAPIs;
import com.eli.oneos.model.http.HttpUtils;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.backup.file.ScanningAlbumThread;
import com.eli.oneos.model.oneos.transfer.OnTransferFileListener;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.transfer.UploadElement;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import www.glinkwin.com.glink.ssudp.SSUDPFileChunk;
import www.glinkwin.com.glink.ssudp.SSUDPManager;

/**
 * OneSpace OS Upload File API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/14.
 */
public class OneOSUploadFileAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSUploadFileAPI.class.getSimpleName();
    private static final int HTTP_UPLOAD_TIMEOUT = 30 * 1000;
    private static final int HTTP_UPLOAD_RENAME_TIMES = 100;
    private static final int HTTP_UPLOAD_RETRY_TIMES = 5;
    private static final int HTTP_BUFFER_SIZE = 1024 * 8;
    private static final double SSUDP_CHUNK_SIZE = 1024 * 1024;
    /**
     * chuck block size: 2mb
     */
    private static final long HTTP_BLOCK_SIZE = 1024 * 1024 * 2;

    private OnTransferFileListener<UploadElement> listener;
    private UploadElement uploadElement;
    private boolean isInterrupt = false;
    private LoginSession loginSession;
    private String pathPosition = null;

    public OneOSUploadFileAPI(LoginSession loginSession, UploadElement element) {
        super(loginSession);
        this.loginSession = loginSession;
        this.uploadElement = element;
    }

    public void setOnUploadFileListener(OnTransferFileListener<UploadElement> listener) {
        this.listener = listener;
    }

    public boolean upload() {
        if (null != listener) {
            listener.onStart(url, uploadElement);
        }

        if (uploadElement.isCheck()) {
            int check = checkExist(uploadElement.getToPath() + uploadElement.getSrcName(), uploadElement.getSize());
            if (check == 1) {
                uploadElement.setState(TransferState.COMPLETE);
            } else if (check == -1) {
                duplicateRename(uploadElement.getToPath() + uploadElement.getSrcName(), uploadElement.getSrcName());
            } else {
                if (LoginManage.getInstance().isHttp()) {
                    doHttpUpload();
                } else {
                    doSSUDPUpload();
                }
            }
        } else {
            if (LoginManage.getInstance().isHttp()) {
                doHttpUpload();
            } else {
                doSSUDPUpload();
            }
        }

        if (null != listener) {
            listener.onComplete(url, uploadElement);
        }

        return uploadElement.getState() == TransferState.COMPLETE;
    }

    int count = 1;
    int index = 1;

    private void duplicateRename(final String path, final String srcName) {
        // String newName = genDuplicateName(srcName, index);
        Map<String, Object> params = new HashMap<>();
        params.put("session", session);
        params.put("cmd", "rename");
        params.put("path", path);
        // params.put("newname", "");

        String url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        try {
            String result = (String) httpUtils.postSync(url, params);
            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "File Attr: " + result);
            JSONObject json = new JSONObject(result);
            boolean ret = json.getBoolean("result");
            if (ret) {
                Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "======Duplicate Rename Success");
                if (LoginManage.getInstance().isHttp()) {
                    doHttpUpload();
                } else {
                    doSSUDPUpload();
                }
            } else {
                Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "======Duplicate Rename Failed");
                if (count <= HTTP_UPLOAD_RENAME_TIMES) {
                    count++;
                    // index = (int) Math.pow(2, count);
                    index = count;
                    duplicateRename(path, srcName);
                } else {
                    Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "======Duplicate Rename " + count + " Times, Skip...");
                    uploadElement.setState(TransferState.FAILED);
                }
            }
        } catch (Exception e) {
            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "****Upload file not exist on server: " + path/*, e*/);
        }
    }

//    private String genDuplicateName(String srcName, int index) {
//        int pos = srcName.lastIndexOf(".");
//        if (pos == -1) {
//            return srcName + "_" + index;
//        }
//
//        String name = srcName.substring(0, pos);
//        return name + "_" + index + srcName.substring(pos, srcName.length());
//    }

    public void stopUpload() {
        isInterrupt = true;
        Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "Upload Stopped");
    }

    /**
     * check if file exist in server
     *
     * @param path    file server path
     * @param srcSize
     * @return 1: exist and do not needs to upload; -1: needs rename old file then upload; 0: file do not exist
     */
    private int checkExist(String path, long srcSize) {
        //String url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        //params.put("session", session);
        params.put("cmd", "attributes");
        params.put("path", path);
        try {
            String result;
            if (OneOSAPIs.isOneSpaceX1()) {
                String url = genOneOSAPIUrl(OneSpaceAPIs.FILE_ATTR);
                params.put("session", session);
                params.put("path", OneOSAPIs.getOneSpaceUid() + path);
                result = (String) httpUtils.postSync(url, params);
            } else {
                String url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
                params.put("path", path);
                result = (String) httpUtils.postSync(url, new RequestBody("manage", session, params));
            }
            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "File Attr: " + result);
            JSONObject json = new JSONObject(result);
            boolean ret = json.getBoolean("result");
            if (ret) {
                long size = json.getJSONObject("data").getLong("size");
                if (size == srcSize) {
                    Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "****Upload file exist on server: " + path);
                    return 1; // exist and do not needs to upload
                } else {
                    return -1; // needs rename old file
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "****Upload file not exist on server: " + path/*, e*/);
        }

        return 0; // file do not exist
    }




    private void doHttpUpload() {
        if (OneOSAPIs.isOneSpaceX1()) {
            url = genOneOSAPIUrl(OneSpaceAPIs.FILE_UPLOAD);
        } else {
            url = genOneOSAPIUrl(OneOSAPIs.FILE_UPLOAD);
        }
        HttpUtils.log(TAG, url, null);

        uploadElement.setState(TransferState.START);
        String session = loginSession.getSession();
        String srcPath = uploadElement.getSrcPath();
        String targetPath = uploadElement.getToPath();

        File uploadFile = new File(srcPath);
        if (!uploadFile.exists()) {
            Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "upload file does not exist");
            uploadElement.setState(TransferState.FAILED);
            uploadElement.setException(TransferException.FILE_NOT_FOUND);
            return;
        }

        long fileLen = uploadFile.length();
        long uploadPosition = 0;
        // Modified to new position, to make sure anim_progress is correct
        uploadElement.setLength(uploadPosition);
        uploadElement.setOffset(uploadPosition);

        String PREFIX = "--";
        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型

        long retry = 0; // exception retry times
        long uploadLen = 0;
        long chunkNum = (long) Math.ceil((double) fileLen / (double) HTTP_BLOCK_SIZE);
        long chunkIndex;
        for (chunkIndex = 0; chunkIndex < chunkNum; chunkIndex++) {
            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "=====>>> BlockIndex:" + chunkIndex + ", BlockNum:" + chunkNum + ", BlockSize:" + HTTP_BLOCK_SIZE);
            long blockUpLen = 0;
            try {
                URL mUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setReadTimeout(HTTP_UPLOAD_TIMEOUT);
                conn.setConnectTimeout(HTTP_UPLOAD_TIMEOUT);
                conn.setDoInput(true); // 允许输入流
                conn.setDoOutput(true); // 允许输出流
                conn.setUseCaches(false); // 不允许使用缓存
                conn.setRequestMethod("POST"); // 请求方式
                conn.setRequestProperty("Charset", HTTP.UTF_8); // 设置编码
                conn.setRequestProperty("connection", "keep-alive");
                conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
                // conn.connect();

                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb = new StringBuffer();

                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"session\"" + LINE_END);
                sb.append(LINE_END);
                sb.append(session);
                sb.append(LINE_END);
                outStream.write(sb.toString().getBytes());
                outStream.flush();

                sb = new StringBuffer();
                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                if (OneOSAPIs.isOneSpaceX1()) {
                    sb.append("Content-Disposition: form-data; name=\"savepath\"" + LINE_END);
                } else {
                    sb.append("Content-Disposition: form-data; name=\"todir\"" + LINE_END);
                }
                sb.append(LINE_END);
                if (OneOSAPIs.isOneSpaceX1() && targetPath.indexOf("storage") == -1) {
                    if (targetPath.indexOf("uid") == -1) {
                        targetPath = OneOSAPIs.getOneSpaceUid() + targetPath;
                    } else {
                        targetPath = "storage/" + targetPath;
                    }
                }
                sb.append(targetPath);
                Log.d(TAG, "topath=" + targetPath);
                sb.append(LINE_END);
                outStream.write(sb.toString().getBytes());
                outStream.flush();

                if (uploadElement.isOverwrite()) {
                    sb = new StringBuffer();
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"overwrite\"" + LINE_END);
                    sb.append(LINE_END);
                    sb.append("1");
                    sb.append(LINE_END);
                    outStream.write(sb.toString().getBytes());
                    outStream.flush();
                }

                sb = new StringBuffer();
                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"chunks\"" + LINE_END);
                sb.append(LINE_END);
                sb.append(chunkNum);
                sb.append(LINE_END);
                outStream.write(sb.toString().getBytes());
                outStream.flush();

                sb = new StringBuffer();
                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"chunk\"" + LINE_END);
                sb.append(LINE_END);
                sb.append(chunkIndex);
                sb.append(LINE_END);
                outStream.write(sb.toString().getBytes());
                outStream.flush();

                sb = new StringBuffer();
                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"name\"" + LINE_END);
                sb.append(LINE_END);
                sb.append(uploadFile.getName());
                sb.append(LINE_END);
                outStream.write(sb.toString().getBytes());
                outStream.flush();

                sb = new StringBuffer();
                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + uploadFile.getName() + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream;charset=" + HTTP.UTF_8 + LINE_END);
                sb.append(LINE_END);
                outStream.write(sb.toString().getBytes());
                outStream.flush();

                RandomAccessFile inputStream = new RandomAccessFile(uploadFile, "r");
                inputStream.seek(chunkIndex * HTTP_BLOCK_SIZE);
                byte[] bytes = new byte[HTTP_BUFFER_SIZE];
                int len;
                int callback = 0; // for upload progress callback
                while (!isInterrupt && (len = inputStream.read(bytes)) != -1) {
                    outStream.write(bytes, 0, len);
                    blockUpLen += len;
                    uploadLen += len;
                    uploadElement.setLength(uploadLen);
                    callback++;
                    if (null != listener && callback == 64) {
                        // callback every 512KB
                        listener.onTransmission(url, uploadElement);
                        callback = 0;
                    }
                    if (blockUpLen >= HTTP_BLOCK_SIZE) {
                        break;
                    }
                }
                inputStream.close();

                if (!isInterrupt) {
                    outStream.write(LINE_END.getBytes());
                    byte[] end = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    outStream.write(end);
                    outStream.flush();
                    outStream.close();

                    int code = conn.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "Http Response Error, code = " + code);
                        uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                        retry++;
                    } else {
                        retry = 0;
                    }
                } else {
                    outStream.close();
                    Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "End, file upload interrupt");
                    break;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.FILE_NOT_FOUND);
            } catch (IOException e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.IO_EXCEPTION);
            } finally {
                if (!isInterrupt && retry > 0) {
                    try {
                        chunkIndex--;
                        uploadLen -= blockUpLen;
                        uploadElement.setLength(uploadLen);
                        Thread.sleep(retry * retry * 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!isInterrupt && retry > HTTP_UPLOAD_RETRY_TIMES) {
                    Logger.p(LogLevel.WARN, Logged.UPLOAD, TAG, "Upload exception: Retry " + HTTP_UPLOAD_RETRY_TIMES + " times, Exit...");
                    break;
                }
            }
        }

        Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "The end of the file upload: FileLen = " + fileLen + ", UploadLen = " + uploadLen);
        if (fileLen == uploadLen) {
            uploadElement.setState(TransferState.COMPLETE);
        } else {
            if (isInterrupt) {
                uploadElement.setState(TransferState.PAUSE);
            } else {
                uploadElement.setState(TransferState.FAILED);
            }
        }
    }

    private void doSSUDPUpload() {
        uploadElement.setState(TransferState.START);
        String session = loginSession.getSession();
        String srcPath = uploadElement.getSrcPath();
        String targetPath = new File(uploadElement.getToPath(), uploadElement.getSrcName()).getPath();
        Log.d(TAG, "targetpath" + targetPath);

        File uploadFile = new File(srcPath);
        if (!uploadFile.exists()) {
            Logger.p(LogLevel.ERROR, Logged.UPLOAD, TAG, "upload file does not exist");
            uploadElement.setState(TransferState.FAILED);
            uploadElement.setException(TransferException.FILE_NOT_FOUND);
            return;
        }

        long fileLen = uploadFile.length();
        long uploadPosition = 0;
        // Modified to new position, to make sure anim_progress is correct
        uploadElement.setLength(uploadPosition);
        uploadElement.setOffset(uploadPosition);

        long chunks = (long) Math.ceil((double) fileLen / SSUDP_CHUNK_SIZE);
        long chunk = (long) Math.floor((double) uploadPosition / SSUDP_CHUNK_SIZE);

        SSUDPManager ssudpManager = SSUDPManager.getInstance();
        SSUDPFileChunk fileChunk = new SSUDPFileChunk(session, targetPath, chunks, chunk);
        int retry = 0;
        long uploadLen = uploadPosition;
        int chunkSize = (int) SSUDP_CHUNK_SIZE;

        try {
            RandomAccessFile inputStream = new RandomAccessFile(uploadFile, "r");
            while (!isInterrupt && fileChunk.chunk < fileChunk.chunks) {
                Log.d(TAG, ">>>> SSUDP upload: chunk=" + chunk + ", chunks=" + chunks);
                inputStream.seek(uploadLen);
                if (fileChunk.chunk == fileChunk.chunks - 1) {
                    fileChunk.body((int) (fileLen - uploadLen));
                } else {
                    fileChunk.body(chunkSize);
                }


                fileChunk.length = inputStream.read(fileChunk.body);
                if (fileChunk.length == -1) {
                    break;
                }

                fileChunk = ssudpManager.ssudpUploadRequest(fileChunk);
                if (fileChunk.result) {
                    fileChunk.chunk++;
                    uploadLen += fileChunk.length;
                    uploadElement.setLength(uploadLen);
                    if (null != listener) {
                        listener.onTransmission(url, uploadElement);
                    }
                } else {
                    retry++;
                    if (retry > 5) {
                        uploadElement.setState(TransferState.FAILED);
                        uploadElement.setException(TransferException.SSUDP_DISCONNECTED);
                        break;
                    }
                }
            }

            inputStream.close();

            Logger.p(LogLevel.DEBUG, Logged.UPLOAD, TAG, "The end of the file upload: FileLen = " + fileLen + ", UploadLen = " + uploadLen);
            if (fileLen == uploadLen) {
                uploadElement.setState(TransferState.COMPLETE);
            } else {
                if (isInterrupt) {
                    uploadElement.setState(TransferState.PAUSE);
                } else {
                    uploadElement.setState(TransferState.FAILED);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            uploadElement.setState(TransferState.FAILED);
            uploadElement.setException(TransferException.FILE_NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
            uploadElement.setState(TransferState.FAILED);
            uploadElement.setException(TransferException.IO_EXCEPTION);
        }

    }


    private void getServerPhotoList(String url, Map<String, String> map, List<ScanningAlbumThread.TmpElemet> photoList) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            params.add(new BasicNameValuePair(key, map.get(key)));
        }

        try {
            HttpPost httpRequest = new HttpPost(url);
            Log.d(TAG, "Url: " + url);
            DefaultHttpClient httpClient = new DefaultHttpClient();

            httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 5000);
            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);

            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            // Log.d(TAG, "Response Code: " +
            // httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String resultStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                httpRequest.abort();

                // Log.d(TAG, "Response: " + resultStr);
                JSONObject jsonObj = new JSONObject(resultStr);
                JSONArray jsonArray = null;
                boolean isRequested = jsonObj.getBoolean("result");
                if (isRequested) {
                    String fileStr = jsonObj.getString("files");
                    if (!fileStr.equals("{}")) {
                        jsonArray = (JSONArray) jsonObj.get("files");
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ScanningAlbumThread.TmpElemet mElemet = new ScanningAlbumThread.TmpElemet();
                            mElemet.setFullName(jsonObject.getString("fullname"));
                            mElemet.setLength(jsonObject.getLong("size"));
                            boolean isDir = jsonObject.getString("type").equals("dir");
                            if (isDir) {
                                Map<String, String> tmpMap = new HashMap<String, String>();
                                tmpMap.put("path", mElemet.getFullName());
                                LoginSession loginSession = LoginManage.getInstance().getLoginSession();
                                tmpMap.put("session", loginSession.toString());
                                Log.d(TAG, "List Path: " + mElemet.getFullName());
                                getServerPhotoList(url, tmpMap, photoList);
                            } else {
                                photoList.add(mElemet);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
