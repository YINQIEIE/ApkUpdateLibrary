package com.yq.downloadtest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yq.library_download.DownloadConfig;
import com.yq.library_download.DownloadListener;
import com.yq.library_download.DownloadUtil;

public class MainActivity extends AppCompatActivity {

    private DownloadUtil downloadUtil;
    private AlertDialog dialog;
    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onStart() {
            showProgressDialog();
        }

        @Override
        public void onDownloading(int progress) {
            dialog.setMessage(getLoadingMsg(progress));
        }

        @Override
        public void onDownloadSuccess() {
            dialog.setMessage("下载成功");
        }

        @Override
        public void onDownloadError(String msg) {
            dialog.setMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String url = "http://hm.hawknet.com.cn/hm_debug_2.0.apk";
        String updateMsg = "new version avaliable";
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp";
        String localName = "hello";
        String version = "2.0.0";
//        DownloadConfig config = new DownloadConfig(url, updateMsg, savePath, localName, version, false, true);
        DownloadConfig config = new DownloadConfig()
                .url(url)
                .updateMsg(updateMsg)
                .savePath(savePath)
                .localName(localName)
                .version(version)
                .forceToUpdate(false)
                .isAppend(true);
        downloadUtil = new DownloadUtil(this);
        downloadUtil.init(config);
        MyNotificationManager manager = new MyNotificationManager(this);
        downloadUtil.setmNotificationManager(manager);
        downloadUtil.setDownloadListener(downloadListener);
        Log.i("download", downloadUtil.getApkFile(config).getAbsolutePath());
//        downloadUtil.showNewVersionDialog();
        downloadUtil.showNewVersionDialog(showNewVersionDialog());
    }

    public Dialog showNewVersionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本更新");
        builder.setMessage("为了更新版本杀了一个程序员祭天！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadUtil.startDownload();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadUtil.ExitApp();
            }
        });
        builder.setCancelable(false);
        return builder.show();
    }

    public void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("下载中");
        builder.setMessage(getLoadingMsg(0));
        builder.setCancelable(false);
        dialog = builder.show();
    }

    public String getLoadingMsg(int progress) {
        return String.format("当前下载进度：%d", progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadUtil.onDestroy();
    }

}
