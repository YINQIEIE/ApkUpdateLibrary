package com.yq.library_download;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import java.io.File;

import static com.yq.library_download.DownloadListener.DOWNLOADING;
import static com.yq.library_download.DownloadListener.DOWNLOAD_ERROR;
import static com.yq.library_download.DownloadListener.DOWNLOAD_START;
import static com.yq.library_download.DownloadListener.DOWNLOAD_SUCCESS;
import static com.yq.library_download.DownloadListener.INSTALL_APK;


public class DownloadUtil {

    private Context mContext;
    private DownloadConfig downloadConfig;//下载信息
    private DownloadListener downloadListener;//下载监听
    private boolean isWifiCheck = true;//是否检查网络类型，默认检查
    private BaseNotificationManager mNotificationManager;
    private NotWifiListener notWifiListener = new NotWifiListener() {
        @Override
        public void showNotice() {
            showNotWifiDialog();
        }

        @Override
        public void continueDownload() {
            doDownload();
        }

        @Override
        public void cancelDownload() {
            if (downloadConfig.isForceToUpdate())
                ExitApp();
            else if (null != notWifiDialog && notWifiDialog.isShowing())
                notWifiDialog.dismiss();
        }
    };
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
                    Log.i("download...", progress + "");
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
                    if (null != mNotificationManager)
                        mNotificationManager.updateTitle("下载完成");
                    openInstallerUI(getApkFile(downloadConfig));
                    break;
                case DOWNLOAD_ERROR:
                    String mm = msg.obj.toString();
                    if (null != downloadListener)
                        downloadListener.onDownloadError(mm);
                    if (null != mNotificationManager)
                        mNotificationManager.updateTitle(mm);
                    break;
                case INSTALL_APK:
                    openInstallerUI(getApkFile(downloadConfig));
                    break;
                default:
                    break;
            }
        }
    };
    //demo
    private NotWifiDialog notWifiDialog;
    private NewVersionDialog newVersionDialog;

    public DownloadUtil(Context mContext) {
        this.mContext = mContext;
    }

    public void init(DownloadConfig config) {
        this.downloadConfig = config;
    }

    /**
     * 默认新版本提示
     */
    public void showNewVersionDialog() {
        buildNewVersionDialog();
    }

    /**
     * 提示的 dialog
     *
     * @param dialog 自己定义
     */
    public void showNewVersionDialog(Dialog dialog) {
        dialog.show();
    }

    /**
     * 开始下载任务
     */
    public void startDownload() {
        if (null == downloadConfig)
            throw new RuntimeException("Method init() should be called first !");
        if (isApkDownloadFinished()) {//已经下载完成
            openInstallerUI(getApkFile(downloadConfig));
        } else {
            if (isWifiCheck && !CommonUtil.isWifi(mContext)) {
                notWifiListener.showNotice();
            } else
                doDownload();
        }
    }

    /**
     * 判断 apk 是否已经下载完成
     *
     * @return true 下载完成
     */
    public boolean isApkDownloadFinished() {
        SharedPreferences sp = SharedPrefUtil.getDownloadSp(mContext);
        String appName = getApkName(downloadConfig);
        File downloadApk = getApkFile(downloadConfig);
        return sp.getBoolean(appName, false) && downloadApk.exists();
    }

    /**
     * 根据 config 里的信息获取 apk file 对象
     *
     * @return apk 对应的 file
     */
    public static File getApkFile(DownloadConfig downloadConfig) {
        String appName = getApkName(downloadConfig);
        return new File(downloadConfig.getSavePath() + File.separator + appName);
    }

    public static String getApkName(DownloadConfig downloadConfig) {
        return downloadConfig.getLocalName() + "-" + downloadConfig.getVersion() + ".apk";
    }

    /**
     * 显示更新提示
     */

    public void buildNewVersionDialog() {
        newVersionDialog = new NewVersionDialog(mContext, downloadConfig.getUpdateMsg(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.tv_sure) {
                    newVersionDialog.dismiss();
                    startDownload();
                } else if (i == R.id.tv_cancel) {
                    if (downloadConfig.isForceToUpdate()) {
                        // 强制更新
                        ExitApp();
                    } else
                        newVersionDialog.dismiss();
                }
            }
        });
        newVersionDialog.setCancelable(!downloadConfig.isForceToUpdate());
        newVersionDialog.setCanceledOnTouchOutside(!downloadConfig.isForceToUpdate());
        newVersionDialog.show();
    }

    private void openInstallerUI(File downloadApk) {
        Intent apkIntent = new Intent();
        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        apkIntent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(downloadApk);
        apkIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(apkIntent);
    }

    /**
     * 非wifi提示
     */
    private void showNotWifiDialog() {
        notWifiDialog = new NotWifiDialog(mContext, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.tv_sure) {
                    notWifiListener.continueDownload();
                } else if (i == R.id.tv_cancel) {
                    notWifiListener.cancelDownload();
                }
            }
        });
        notWifiDialog.setCanceledOnTouchOutside(!downloadConfig.isForceToUpdate());
        notWifiDialog.setCancelable(!downloadConfig.isForceToUpdate());
        notWifiDialog.show();

    }

    public static void ExitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
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
     * @param what
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
    }

    public void setmNotificationManager(BaseNotificationManager mNotificationManager) {
        this.mNotificationManager = mNotificationManager;
    }
}
