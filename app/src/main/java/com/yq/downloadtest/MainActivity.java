package com.yq.downloadtest;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yq.library_download.DownloadConfig;
import com.yq.library_download.DownloadUtil;

public class MainActivity extends AppCompatActivity {

    private DownloadUtil downloadUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String url = "http://hm.hawknet.com.cn/hm_debug_2.0.apk";
        String updateMsg = "new version avaliable";
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp";
        String localName = "hello";
        String version = "2.0.0";
        DownloadConfig config = new DownloadConfig(url, updateMsg, savePath, localName, version, false, false);
        downloadUtil = new DownloadUtil(this);
        downloadUtil.init(config);
        MyNotificationManager manager = new MyNotificationManager(this);
        downloadUtil.setmNotificationManager(manager);
        Log.i("download", downloadUtil.getApkFile(config).getAbsolutePath());
    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadUtil.showNewVersionDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadUtil.onDestroy();
    }

}
