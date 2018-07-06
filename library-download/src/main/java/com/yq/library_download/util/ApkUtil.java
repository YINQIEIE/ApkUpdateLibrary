package com.yq.library_download.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.yq.library_download.DownloadConfig;

import java.io.File;

/**
 * Created by Administrator on 2018/4/16.
 */

public class ApkUtil {
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
     * 打开 Apk 安装界面
     *
     * @param downloadApk apk 对应的 file 对象
     */
    public static void openInstallerUI(Context mContext, File downloadApk) {
        Intent apkIntent = getInstallIntent(downloadApk);
        mContext.startActivity(apkIntent);
    }

    public static Intent getInstallIntent(File downloadApk) {
        Intent apkIntent = new Intent();
        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        apkIntent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(downloadApk);
        apkIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        return apkIntent;
    }

}
