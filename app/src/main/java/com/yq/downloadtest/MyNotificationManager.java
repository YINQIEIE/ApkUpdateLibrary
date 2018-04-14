package com.yq.downloadtest;

import android.content.Context;

import com.yq.library_download.BaseNotificationManager;

/**
 * Created by Administrator on 2018/4/13.
 * 主要设置通知图标
 */

public class MyNotificationManager extends BaseNotificationManager {

    public MyNotificationManager(Context mContext) {
        super(mContext);
    }

    @Override
    protected int getSmallIconId() {
        return R.mipmap.ic_launcher;
    }
}
