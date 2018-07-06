package com.yq.library_download;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.yq.library_download.dialog.NewVersionDialog;
import com.yq.library_download.dialog.NotWifiDialog;
import com.yq.library_download.listener.DownloadListener;
import com.yq.library_download.util.CommonUtil;
import com.yq.library_download.util.DownloadUtil;
import com.yq.library_download.util.SharedPrefUtil;

import java.io.File;

import static com.yq.library_download.util.ApkUtil.getApkFile;
import static com.yq.library_download.util.ApkUtil.getApkName;
import static com.yq.library_download.util.ApkUtil.openInstallerUI;

/**
 * Created by Administrator on 2018/4/16.
 * 下载管理类
 */

public class DownLoadManager {

    private Context mContext;
    private DownloadConfig downloadConfig;//下载信息
    private DownloadUtil downloadUtil;//下载类
    private boolean isWifiCheck = true;//是否检查非 wifi 的情况，默认检查

    //版本更新提示 dialog 和非 wifi 提示
    private Dialog notWifiDialog;
    private NewVersionDialog newVersionDialog;

    public DownLoadManager(Context mContext, @NonNull DownloadConfig downloadConfig) {
        this(mContext, downloadConfig, null);
    }

    public DownLoadManager(Context mContext, DownloadConfig downloadConfig, @Nullable DownloadListener downloadListener) {
        this(mContext, downloadConfig, downloadListener, null);
    }

    public DownLoadManager(Context mContext, DownloadConfig downloadConfig, @Nullable DownloadListener downloadListener, @Nullable BaseNotificationManager notificationManager) {
        this.mContext = mContext;
        this.downloadConfig = downloadConfig;
        downloadUtil = new DownloadUtil(mContext, downloadConfig, downloadListener, notificationManager);
    }

    /**
     * 默认新版本提示
     */
    public void showNewVersionDialog() {
        buildNewVersionDialog();
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
                    newVersionDialog.dismiss();
                    if (downloadConfig.isForceToUpdate())
                        // 强制更新
                        ExitApp();
                }
            }
        });
        newVersionDialog.setCancelable(!downloadConfig.isForceToUpdate());
        newVersionDialog.setCanceledOnTouchOutside(!downloadConfig.isForceToUpdate());
        newVersionDialog.show();
    }

    /**
     * 非wifi提示
     */
    private void showNotWifiDialog() {
        notWifiDialog = new NotWifiDialog(mContext, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notWifiDialog.dismiss();
                int i = v.getId();
                if (i == R.id.tv_sure) {
                    doDownload();
                } else if (i == R.id.tv_cancel) {
                    if (downloadConfig.isForceToUpdate())
                        ExitApp();
                }
            }
        });
        notWifiDialog.setCanceledOnTouchOutside(!downloadConfig.isForceToUpdate());
        notWifiDialog.setCancelable(!downloadConfig.isForceToUpdate());
        notWifiDialog.show();

    }

    /**
     * 开始下载任务
     */
    public void startDownload() {
        if (null == downloadConfig)
            throw new NullPointerException("Error : downloadConfig is null !");
        if (isApkDownloadFinished()) {//已经下载完成
            openInstallerUI(mContext, getApkFile(downloadConfig));
        } else {
            if (isWifiCheck && !CommonUtil.isWifi(mContext)) {
                if (null == notWifiDialog)//设置了非 wifi dialog
                    notWifiDialog.show();
                else
                    showNotWifiDialog();
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
     * 强制退出 APP
     */
    public static void ExitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 设置非 wifi 提示 dialog
     *
     * @param notWifiDialog 要显示的提示 dialog
     */
    public void setNotWifiDialog(Dialog notWifiDialog) {
        this.notWifiDialog = notWifiDialog;
    }

    public void setmNotificationManager(BaseNotificationManager mNotificationManager) {
        downloadUtil.setmNotificationManager(mNotificationManager);
    }

    /**
     * 开始下载
     */
    public void doDownload() {
        downloadUtil.doDownload();
    }

    /**
     * 非 wifi 继续下载
     */
    public void notWifiContinueDownload() {
        doDownload();
    }

    /**
     * 非 wifi 取消下载
     */
    public void notWifiCancelDownload() {
        if (downloadConfig.isForceToUpdate())
            ExitApp();
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        downloadUtil.setDownloadListener(downloadListener);
    }

    public void onDestroy() {
        downloadUtil.onDestroy();
    }

    public DownloadConfig getDownloadConfig() {
        return downloadConfig;
    }
}
