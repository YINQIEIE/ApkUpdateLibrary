package com.yq.library_download.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.yq.library_download.BaseNotificationManager;
import com.yq.library_download.service.DownLoadService;
import com.yq.library_download.DownloadConfig;
import com.yq.library_download.listener.DownloadListener;

import static com.yq.library_download.listener.DownloadListener.DOWNLOADING;
import static com.yq.library_download.listener.DownloadListener.DOWNLOAD_ERROR;
import static com.yq.library_download.listener.DownloadListener.DOWNLOAD_START;
import static com.yq.library_download.listener.DownloadListener.DOWNLOAD_SUCCESS;
import static com.yq.library_download.listener.DownloadListener.INSTALL_APK;
import static com.yq.library_download.util.ApkUtil.getApkFile;
import static com.yq.library_download.util.ApkUtil.getInstallIntent;


public class DownloadUtil {

    private Context mContext;
    private DownloadConfig downloadConfig;//下载信息
    private DownloadListener downloadListener;//下载监听
    private BaseNotificationManager mNotificationManager;
    //版本更新相关
    public boolean mIsBind;
    private Messenger rMessenger = null;
    private Messenger mMessenger = null;
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case DOWNLOAD_START:
                    if (null != downloadListener)
                        downloadListener.onStart();
                    if (null != mNotificationManager)
                        mNotificationManager.showNotification();
                    break;
                case DOWNLOADING:
                    int progress = Integer.parseInt(msg.obj.toString());
                    if (null != downloadListener) {
                        downloadListener.onDownloading(progress);
                    }
                    if (null != mNotificationManager) {
                        mNotificationManager.updateTitle("下载中...");
                        mNotificationManager.updateProgress(progress);
                    }
                    break;
                case DOWNLOAD_SUCCESS:
                    if (null != downloadListener)
                        downloadListener.onDownloadSuccess();
                    if (null != mNotificationManager) {
                        mNotificationManager.updateTitle("下载完成");
                        mNotificationManager.updatePendingIntent(getInstallIntent(getApkFile(downloadConfig)));
                    }
                    ApkUtil.openInstallerUI(mContext, ApkUtil.getApkFile(downloadConfig));
                    break;
                case DOWNLOAD_ERROR:
                    String mm = msg.obj.toString();
                    if (null != downloadListener)
                        downloadListener.onDownloadError(mm);
                    if (null != mNotificationManager)
                        mNotificationManager.updateTitle(mm);
                    break;
                case INSTALL_APK:
                    ApkUtil.openInstallerUI(mContext, ApkUtil.getApkFile(downloadConfig));
                    break;
                default:
                    break;
            }
        }
    };

    public DownloadUtil(Context mContext, DownloadConfig config) {
        this(mContext, config, null);
    }

    public DownloadUtil(Context mContext, DownloadConfig config, DownloadListener downloadListener) {
        this(mContext, config, downloadListener, null);
    }

    public DownloadUtil(Context mContext, DownloadConfig config, DownloadListener downloadListener, BaseNotificationManager notificationManager) {
        this.mContext = mContext;
        this.downloadConfig = config;
        this.downloadListener = downloadListener;
        this.mNotificationManager = notificationManager;
    }


    /**
     * 开启下载服务
     */
    public void doDownload() {
        doBindService();
    }

    public void doBindService() {
        Intent intent = new Intent(mContext, DownLoadService.class);
        intent.setPackage("com.yq.library_download");
        Bundle bundle = new Bundle();
        bundle.putSerializable("config", downloadConfig);
        intent.putExtras(bundle);
        // Context.BIND_AUTO_CREATE表明只要绑定存在，就自动建立
        // Service；同时也告知Android系统，这个Service的重要程度与调用者相同，
        // 除非考虑终止调用者，否则不要关闭这个Service
        mIsBind = mContext.bindService(intent, serConn, Context.BIND_AUTO_CREATE);// if bind success return true
    }


    public ServiceConnection serConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rMessenger = null;
            mMessenger = null;
            if (null != mhandler) {
                mhandler.removeCallbacksAndMessages(null);
                mhandler = null;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            rMessenger = new Messenger(service);// get the object of remote service
            mMessenger = new Messenger(mhandler);// initial the object of local
            mhandler.sendEmptyMessage(DOWNLOAD_START);
            // service
            sendMessage(DownLoadService.DOWNLOAD);
        }
    };


    /**
     * 向远程Messenger发送消息开始下载
     *
     * @param what 消息值
     */
    private void sendMessage(int what) {
        Message msg = Message.obtain(null, what);
        msg.replyTo = mMessenger;
        try {
            rMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void onDestroy() {
        if (mIsBind)
            mContext.unbindService(serConn);
        if (null != mNotificationManager)
            mNotificationManager.cancel();
    }

    public void setmNotificationManager(BaseNotificationManager mNotificationManager) {
        this.mNotificationManager = mNotificationManager;
    }
}
