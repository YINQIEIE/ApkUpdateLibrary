package com.yq.library_download.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by yiqile-21 on 2016/5/31.
 */
public class SharedPrefUtil {

    public static final String KEY_VERSION = "versionInfo";
    public static final String KEY_OTHER = "other";
    private static SharedPreferences sp;

    /**
     * 根据SP的名字获取SP
     *
     * @param context 上下文
     * @param spName  SP的名字
     */
    private static void getSPInstance(Context context, String spName) {
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }


    /**
     * 保存版本信息到本地，判断是否需要
     *
     * @param context
     * @param version
     */
    public static void saveVersion(Context context, float version) {
        getSPInstance(context, KEY_VERSION);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat("version", version);
        editor.commit();
    }

    /**
     * 获取本地保存的版本号
     *
     * @param context
     * @return
     */
    public static float getSavedVersion(Context context) {
        getSPInstance(context, KEY_VERSION);
        float version = sp.getFloat("version", 0);
        return version;
    }

    /**
     * 根据key清除对应value
     *
     * @param context
     * @param key
     */
    public static void removeValue(Context context, String key) {

        getSPInstance(context, KEY_OTHER);

        SharedPreferences.Editor editor = sp.edit();

        editor.remove(key);

        editor.commit();

    }

    /**
     * 获取版本更新下载 apk 包信息的 sp
     *
     * @param context
     * @return
     */
    public static SharedPreferences getDownloadSp(Context context) {

        return context.getSharedPreferences("down_info", Context.MODE_PRIVATE);

    }

    /**
     * 清除其他版本是否下载完成的信息，防止测试时版本来回升降出现安装包解析错误的问题
     * 不清除问题举例：
     * 如：测试时检测到新版本为 2 ，完整下载 apk 包但不安装，此时 SharedPreference 里保存的版本 2 已下载完成
     * 此时，将数据库新版本设置为 3 ，再次下载安装包（可不下载完），下载之前版本 2 对应的安装包已经被删除
     * 再将数据库新版本设置为 2 ，再次下载安装包，在下载完成之前强制停止下载
     * 然后再次启动 APP ，此时根据判断 版本 2 对应的安装包存在，而且保存的信息是版本 2 已经下载完成，如果此时
     * 启动 APP 安装界面，就会出现安装包解析错误的情况
     */
    public static void clearDownloadSPInfo(Context context) {
        SharedPreferences sp = getDownloadSp(context);
        Map<String, ?> key_values = sp.getAll();
        for (String key : key_values.keySet()) {
            sp.edit().remove(key).commit();
        }
    }

}
