package com.yq.downloadtest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yq.library_download.DownLoadManager;
import com.yq.library_download.DownloadConfig;
import com.yq.library_download.listener.DownloadListener;

import java.util.Locale;

import static com.yq.library_download.DownLoadManager.ExitApp;

public class MainActivity extends AppCompatActivity {

    private AlertDialog dialog;
    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onStart() {
            showProgressDialog();
        }

        @Override
        public void onDownloading(int progress) {
            Log.i("download", "progress = " + progress);
            dialog.setMessage(getLoadingMsg(progress));
            if (!dialog.isShowing())
                dialog.show();
        }

        @Override
        public void onDownloadSuccess() {
            dialog.setTitle("下载完成");
        }

        @Override
        public void onDownloadError(String msg) {
            dialog.setMessage(msg);
        }
    };
    private DownLoadManager downloadManager;
    private DownloadConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getRw().watch(this);
        String url = "http://hm.hawknet.com.cn/hm_debug_2.0.apk";
        String updateMsg = "new version avaliable";
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp";
        String localName = "hello";
        String version = "2.0.0";
        config = new DownloadConfig()
                .url(url)
                .updateMsg(updateMsg)
                .savePath(savePath)
                .localName(localName)
                .version(version)
                .forceToUpdate(false)
                .isAppend(true);
        downloadManager = new DownLoadManager(this, config);
        MyNotificationManager notificationManager = new MyNotificationManager(this);
        downloadManager.setmNotificationManager(notificationManager);
        downloadManager.setNotWifiDialog(getNotWifiDialog());
        downloadManager.setDownloadListener(downloadListener);
//        downloadManager.showNewVersionDialog();
        showNewVersionDialog();
    }

    public Dialog showNewVersionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本更新");
        builder.setMessage("为了更新版本杀了一个程序员祭天！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadManager.startDownload();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (config.isForceToUpdate())
                    ExitApp();
            }
        });
        builder.setCancelable(false);
        return builder.show();
    }

    public Dialog getNotWifiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前为移动网络，继续下载可能会产生流量费");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadManager.startDownload();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (config.isForceToUpdate())
                    ExitApp();
            }
        });
        builder.setCancelable(false);
        return builder.create();
    }

    public void showProgressDialog() {
        AlertDialog.Builder progressBuilder = new AlertDialog.Builder(this);
        progressBuilder.setTitle("下载中");
        progressBuilder.setMessage(getLoadingMsg(0));
        progressBuilder.setCancelable(false);
        dialog = progressBuilder.show();
    }

    public String getLoadingMsg(int progress) {
        return String.format(Locale.getDefault(), "当前下载进度：%d", progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadManager.onDestroy();
    }

}
