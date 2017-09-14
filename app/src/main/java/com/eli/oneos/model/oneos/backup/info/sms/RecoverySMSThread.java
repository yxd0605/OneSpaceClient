package com.eli.oneos.model.oneos.backup.info.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.eli.oneos.MyApplication;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.BackupInfoKeeper;
import com.eli.oneos.model.log.LogLevel;
import com.eli.oneos.model.log.Logged;
import com.eli.oneos.model.log.Logger;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.api.OneOSDownloadFileAPI;
import com.eli.oneos.model.oneos.backup.info.BackupInfoException;
import com.eli.oneos.model.oneos.backup.info.BackupInfoStep;
import com.eli.oneos.model.oneos.backup.info.BackupInfoType;
import com.eli.oneos.model.oneos.backup.info.OnBackupInfoListener;
import com.eli.oneos.model.oneos.transfer.DownloadElement;
import com.eli.oneos.model.oneos.transfer.OnTransferFileListener;
import com.eli.oneos.model.oneos.transfer.TransferException;
import com.eli.oneos.model.oneos.transfer.TransferState;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/26.
 */
public class RecoverySMSThread extends Thread {
    private static final String TAG = BackupSMSThread.class.getSimpleName();
    private static final boolean IS_LOG = Logged.BACKUP_SMS;

    private static final BackupInfoType TYPE = BackupInfoType.RECOVERY_SMS;

    private OnBackupInfoListener mListener = null;
    private BackupInfoException exception = null;
    private LoginSession loginSession = null;
    private Context context;

    public RecoverySMSThread(OnBackupInfoListener mListener) {
        if (null == mListener) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "BackupInfoListener is NULL");
            new Throwable(new NullPointerException("BackupInfoListener is NULL"));
            return;
        }
        this.mListener = mListener;
        context = MyApplication.getAppContext();
        loginSession = LoginManage.getInstance().getLoginSession();
    }

    @Override
    public void run() {
        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Start Recovery SMS");
        if (null != mListener) {
            mListener.onStart(TYPE);
        }

        if (downloadSMS()) {
            importSMS();
        }

        if (null == exception) {
            long time = System.currentTimeMillis();
            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Recovery SMS Success, Update database: " + time);
            BackupInfoKeeper.update(loginSession.getUserInfo().getId(), BackupInfoType.RECOVERY_SMS, time);
        }

        if (mListener != null) {
            mListener.onComplete(TYPE, exception);
        }

        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "Complete Recovery SMS");
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
    }

    private boolean downloadSMS() {
        String path = Constants.BACKUP_INFO_ONEOS_ROOT_DIR + Constants.BACKUP_SMS_FILE_NAME;
        OneOSFile file = new OneOSFile();
        file.setPath(path);
        file.setName(Constants.BACKUP_SMS_FILE_NAME);

        String targetPath = context.getCacheDir().getAbsolutePath();
        DownloadElement downloadElement = new DownloadElement(file, targetPath);
        downloadElement.setCheck(false);
        OneOSDownloadFileAPI downloadFileAPI = new OneOSDownloadFileAPI(loginSession, downloadElement);
        downloadFileAPI.setOnDownloadFileListener(new OnTransferFileListener<DownloadElement>() {
            @Override
            public void onStart(String url, DownloadElement element) {
                if (null != mListener) {
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, 0);
                }
            }

            @Override
            public void onTransmission(String url, DownloadElement element) {
                if (null != mListener) {
                    int progress = (int) (((float) element.getLength() / (float) element.getSize()) * 100);
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, progress);
                }
            }

            @Override
            public void onComplete(String url, DownloadElement element) {
                if (null != mListener) {
                    if (element.getState() == TransferState.COMPLETE) {
                        mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, 100);
                    } else {
                        if (element.getException() == TransferException.SERVER_FILE_NOT_FOUND) {
                            exception = BackupInfoException.NO_RECOVERY;
                        } else {
                            exception = BackupInfoException.DOWNLOAD_ERROR;
                        }
                    }
                }
            }
        });

        return downloadFileAPI.download();
    }

    /**
     * Importing SMS from the server to phone
     */
    private boolean importSMS() {
        Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "----Start Import SMS----");

        List<SmsItem> smsItems;
        /** import SMS to phone */
        ContentResolver conResolver = context.getContentResolver();
        /**
         * 放一个解析xml文件的模块
         */
        smsItems = this.getSmsItemsFromXml();
        if (smsItems == null || smsItems.size() == 0) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "---- SMS List is NULL ----");
            return false;
        }
        int total = smsItems.size();
        int write = 0;
        Logger.p(LogLevel.INFO, IS_LOG, TAG, "---- Start to import SMS ----");

        for (SmsItem item : smsItems) {
            write++;
            Logger.p(LogLevel.INFO, IS_LOG, TAG, "----Total SMS = " + total + " ; Import SMS : " + write);

            // 判断短信数据库中是否已包含该条短信，如果有，则不需要恢复
            try {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "SMS Date: " + item.getDate());
                Cursor cursor = conResolver.query(Uri.parse("content://sms"),
                        new String[]{SmsField.DATE}, SmsField.DATE + "=?",
                        new String[]{item.getDate()}, null);

                if (!cursor.moveToFirst()) {// 没有该条短信
                    ContentValues values = new ContentValues();
                    values.put(SmsField.ADDRESS, item.getAddress());
                    // 如果是空字符串说明原来的值是null，所以这里还原为null存入数据库
                    values.put(SmsField.PERSON,
                            item.getPerson().equals("") ? null : item.getPerson());
                    values.put(SmsField.DATE, item.getDate());
                    values.put(SmsField.PROTOCOL,
                            item.getProtocol().equals("") ? null : item.getProtocol());
                    values.put(SmsField.READ, item.getRead());
                    values.put(SmsField.STATUS, item.getStatus());
                    values.put(SmsField.TYPE, item.getType());
                    values.put(SmsField.REPLY_PATH_PRESENT, item.getReply_path_present()
                            .equals("") ? null : item.getReply_path_present());
                    values.put(SmsField.BODY, item.getBody());
                    values.put(SmsField.LOCKED, item.getLocked());
                    values.put(SmsField.ERROR_CODE, item.getError_code());
                    values.put(SmsField.SEEN, item.getSeen());
                    conResolver.insert(Uri.parse("content://sms"), values);
                } else {
                    Logger.p(LogLevel.INFO, IS_LOG, TAG, "---- Skip import ");
                }
                cursor.close();
                setProgress(write, total);
            } catch (Exception e) {
                Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Import Sms Exception", e);
            }

            Logger.p(LogLevel.DEBUG, IS_LOG, TAG, "----Total SMS = " + total + " ; Import SMS : " + write);
        }

        return true;
    }

    /**
     * parse xml file of SMS
     */
    private List<SmsItem> getSmsItemsFromXml() {
        List<SmsItem> mSmsList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        String absolutePath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.BACKUP_SMS_FILE_NAME;
        File file = new File(absolutePath);
        if (!file.exists()) {
            exception = BackupInfoException.NO_RECOVERY;
            return null;
        }
        try {
            FileInputStream inputStream = new FileInputStream(file);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream, "UTF-8");
            Element rootElement = document.getDocumentElement();
            NodeList nodeList = rootElement.getElementsByTagName("item");
            // Log.d(TAG, "====NodeList size: " + nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element childElement = (Element) nodeList.item(i);
                NamedNodeMap mChildMap = childElement.getAttributes();

                // Log.d(TAG, "====Child NodeList size: " + mChildMap.getLength());
                SmsItem smsItem = new SmsItem();
                for (int j = 0; j < mChildMap.getLength(); j++) {
                    Attr itemAttr = (Attr) mChildMap.item(j);
                    String nodeName = itemAttr.getNodeName().trim();
                    String nodeValue = itemAttr.getNodeValue();

                    if (nodeName.equals(SmsField.ADDRESS)) {
                        smsItem.setAddress(nodeValue);
                    } else if (nodeName.equals(SmsField.PERSON)) {
                        smsItem.setPerson(nodeValue);
                    } else if (nodeName.equals(SmsField.DATE)) {
                        smsItem.setDate(nodeValue);
                    } else if (nodeName.equals(SmsField.PROTOCOL)) {
                        smsItem.setProtocol(nodeValue);
                    } else if (nodeName.equals(SmsField.READ)) {
                        smsItem.setRead(nodeValue);
                    } else if (nodeName.equals(SmsField.STATUS)) {
                        smsItem.setStatus(nodeValue);
                    } else if (nodeName.equals(SmsField.TYPE)) {
                        smsItem.setType(nodeValue);
                    } else if (nodeName.equals(SmsField.REPLY_PATH_PRESENT)) {
                        smsItem.setReply_path_present(nodeValue);
                    } else if (nodeName.equals(SmsField.BODY)) {
                        smsItem.setBody(nodeValue);
                    } else if (nodeName.equals(SmsField.LOCKED)) {
                        smsItem.setLocked(nodeValue);
                    } else if (nodeName.equals(SmsField.ERROR_CODE)) {
                        smsItem.setError_code(nodeValue);
                    } else if (nodeName.equals(SmsField.SEEN)) {
                        smsItem.setSeen(nodeValue);
                    }
                }

                if (smsItem != null) {
                    mSmsList.add(smsItem);
                }
            }
        } catch (Exception e) {
            Logger.p(LogLevel.ERROR, IS_LOG, TAG, "Parse Sms Xml", e);
            exception = BackupInfoException.ERROR_IMPORT;
            return null;
        }

        return mSmsList;
    }

    /**
     * set import SMS progress_sync
     */
    private void setProgress(long write, long total) {
        Logger.p(LogLevel.INFO, IS_LOG, TAG, "ExportProgress: total = " + total + " ; write = " + write);
        int progress = (int) (((float) write / (float) total) * 100);
        if (null != mListener) {
            mListener.onBackup(TYPE, BackupInfoStep.IMPORT, progress);
        }
    }

}