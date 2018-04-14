package com.yq.library_download;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Administrator on 2018/4/13.
 * 要显示 notification 需要继承这个类，实现设置通知图标的方法
 */

public abstract class BaseNotificationManager {

    private Context mContext;
    public NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    //Notification 的 ID
    int notifyId = 102;

    public BaseNotificationManager(Context mContext) {
        this.mContext = mContext;
        initNotification();
    }

    private void initNotification() {
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("新版本开始下载")
                .setContentIntent(getDefalutIntent())
                // .setNumber(number)//显示数量
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)// 设置该通知优先级
                .setAutoCancel(true)// 设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)// true，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.FLAG_AUTO_CANCEL)// 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                // Notification.DEFAULT_ALL Notification.DEFAULT_SOUND 添加声音 //
                // requires VIBRATE permission
                .setSmallIcon(getSmallIconId());
    }

    /**
     * 初始化通知栏
     */
    public void showNotification() {
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    protected abstract int getSmallIconId();

    /**
     * 获取默认的pendingIntent,为了防止2.3及以下版本报错
     * flags属性: 在顶部常驻:Notification.FLAG_ONGOING_EVENT 点击去除：
     * Notification.FLAG_AUTO_CANCEL
     */
    public PendingIntent getDefalutIntent() {
        Intent resultIntent = new Intent(mContext, ((Activity) mContext).getClass());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void updateProgress(int progress) {
        mBuilder.setProgress(100, progress, false); // 这个方法是显示进度条
        mBuilder.setContentText(progress + "%");
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    public void updateTitle(String msg) {
        mBuilder.setContentTitle(msg);
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    public void updatePendingIntent(Intent installIntent) {
        PendingIntent contextIntent = PendingIntent.getActivity(mContext, 0, installIntent, 0);
        mBuilder.setContentIntent(contextIntent);
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

}
