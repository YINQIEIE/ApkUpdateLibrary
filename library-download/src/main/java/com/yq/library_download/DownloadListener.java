package com.yq.library_download;

/**
 * Created by Administrator on 2018/4/13.
 */

public interface DownloadListener {

    int DOWNLOAD_START = 0;
    int DOWNLOADING = 1;
    int DOWNLOAD_SUCCESS = 2;
    int DOWNLOAD_ERROR = 3;
    int INSTALL_APK = 4;

    void onStart();

    void onDownloading(int progress);

    void onDownloadSuccess();

    void onDownloadError(String msg);
}
