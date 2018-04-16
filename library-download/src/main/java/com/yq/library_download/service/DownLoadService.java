package com.yq.library_download.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.yq.library_download.DownloadConfig;
import com.yq.library_download.util.SharedPrefUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.yq.library_download.listener.DownloadListener.DOWNLOADING;
import static com.yq.library_download.listener.DownloadListener.DOWNLOAD_ERROR;
import static com.yq.library_download.listener.DownloadListener.DOWNLOAD_SUCCESS;
import static com.yq.library_download.util.ApkUtil.getApkFile;
import static com.yq.library_download.util.ApkUtil.getApkName;
import static com.yq.library_download.util.SharedPrefUtil.getDownloadSp;

//此类用于自动更新
public class DownLoadService extends Service {

    public static final int DOWNLOAD = 0;
    private DownloadConfig downloadConfig;
    private String url = null;
    public static String APPNAME_PREFIX = "hm-";
    public static String FILE_SUFFIX = ".apk";
    private String appName = "newApp.apk";
    private HttpURLConnection connection;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isDownloading = false;
    private int fileLength;//文件总长
    private int downedFileLength = 0;//已下载文件长度
    private Messenger cMessenger;//发送消息给 UI 的 messenger

    private boolean continueDownload = true;//是否继续下载标志位，unbind是设置为false
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DOWNLOAD:
                    if (isDownloading) {
                        return;
                    }
                    cMessenger = msg.replyTo;// get the messenger of client
                    downedFileLength = 0;
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                downloadFile(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    break;
                default:
                    break;
            }
        }
    };

    private Messenger mMessenger = new Messenger(mHandler);// It's the messenger of server

    /**
     * 更新进度的定时任务
     */
    private CountDownTimer notificationUpdateTimer = new CountDownTimer(Integer.MAX_VALUE, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            setNotify(fileLength, downedFileLength);
            if (fileLength == downedFileLength)
                this.cancel();
        }

        @Override
        public void onFinish() {
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        downloadConfig = (DownloadConfig) intent.getExtras().get("config");
        url = downloadConfig.getUrl();
        appName = getApkName(downloadConfig);
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (null != notificationUpdateTimer)
            notificationUpdateTimer.cancel();
        continueDownload = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    /**
     * 设置下载进度
     */
    public void setNotify(int max, int progress) {
        if (null != cMessenger) {
            sendMsgToCMessenger(DOWNLOADING, progress * 100 / max);
            if (max == progress)
                sendMsgToCMessenger(DOWNLOAD_SUCCESS, "下载完成");
        }
    }


    /**
     * 下载文件
     * <p>
     * 文件的保存路径和和文件名 通过Context.getExternalFilesDir()方法可以获取到
     * SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
     *
     * @param urlString 下载地址
     */
    private void downloadFile(String urlString) {
        try {
            String dirPath = downloadConfig.getSavePath();
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String apkPath = getApkFile(downloadConfig).getAbsolutePath();
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                for (File file : dir.listFiles()) {//删除以前的文件，防止占用手机存储
                    file.delete();
                }
                apkFile.createNewFile();
                SharedPrefUtil.clearDownloadSPInfo(this);
            } else {//文件已经存在
                SharedPreferences sp = getDownloadSp(this);
                boolean isDownFinish = sp.getBoolean(appName, false);//是否已经下载完成
                if (isDownFinish) {//已经下载完成直接打开app
                    sendMsgToCMessenger(DOWNLOAD_SUCCESS, "下载完成");
                    return;
                }
                downedFileLength = downloadConfig.isAppend() ? (int) apkFile.length() : 0;//没有下载完成，获取已经下载的文件大小
            }
            startDownloadTask(urlString, apkPath);
        } catch (IOException e) {
            sendMsgToCMessenger(DOWNLOAD_ERROR, "下载失败");
            if (null != notificationUpdateTimer) notificationUpdateTimer.cancel();//取消更新进度的倒计时
        }
    }

    private void sendMsgToCMessenger(int what, Object obj) {
        try {
            Message message = Message.obtain();
            message.what = what;
            message.obj = obj;
            if (null != cMessenger)
                cMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始下载
     *
     * @param urlString 下载链接
     * @param apkPath   下载文件路径
     * @throws IOException 文件读写异常
     */

    private void startDownloadTask(String urlString, String apkPath) {
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);//连接超时 5s
            connection.setReadTimeout(10 * 1000);//读取超时
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + downedFileLength + "-");// 设置开始下载的位置
            fileLength = connection.getContentLength();//文件大小，用于设置下载进度 注：此处获取到的大小为文件总大小减去已经下载的大小
            fileLength += downedFileLength;
            inputStream = connection.getInputStream();
            notificationUpdateTimer.start();
            outputStream = new FileOutputStream(new File(apkPath), downloadConfig.isAppend());
            byte[] buffer = new byte[1024 * 4];
            isDownloading = true;
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                if (!continueDownload) return;
                downedFileLength += count;
                outputStream.write(buffer, 0, count);
            }
            saveDownInfo(appName);//保存下载完成的信息
            isDownloading = false;

        } catch (MalformedURLException e1) {
            sendMsgToCMessenger(DOWNLOAD_ERROR, "下载出错");
            Log.i("download", "downloading 地址错误");

        } catch (IOException e) {
            sendMsgToCMessenger(DOWNLOAD_ERROR, "下载失败");
            Log.i("download", "downloading IO异常");
            if (null != notificationUpdateTimer) notificationUpdateTimer.cancel();//取消更新进度的倒计时
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != connection)
                connection.disconnect();
        }
    }

    /**
     * 下载完成后保存新版本下载信息为完成
     * 同时清除掉之前旧版本信息
     *
     * @param appName 先 apk 文件的名字
     */
    private void saveDownInfo(String appName) {
        SharedPreferences sp = getDownloadSp(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(APPNAME_PREFIX + SharedPrefUtil.getSavedVersion(this) + FILE_SUFFIX);//清除上个版本保存的信息
        editor.putBoolean(appName, true);
        editor.commit();
    }

}
