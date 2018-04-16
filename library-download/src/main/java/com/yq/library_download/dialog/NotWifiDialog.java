package com.yq.library_download.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yq.library_download.R;


/**
 * Created by yiqile-21 on 2016/7/26.
 */
public class NotWifiDialog extends Dialog {

    private Context context;
    private TextView tv_title;//标题
    private TextView tv_content;//提示内容
    private TextView tv_sure;//確定
    private TextView tv_cancel;//取消
    private String title;//标题文字
    private String content;//提示内容
    private View.OnClickListener clickListener;


    public NotWifiDialog(Context context) {
        super(context, R.style.NobackDialog);
        this.context = context;
    }

    public NotWifiDialog(Context context, View.OnClickListener clickListener) {
        super(context, R.style.NobackDialog);
        this.context = context;
        this.clickListener = clickListener;

    }

    public NotWifiDialog(Context context, String title, String content, View.OnClickListener clickListener) {
        super(context, R.style.NobackDialog);
        this.context = context;
        this.title = title;
        this.content = content;
        this.clickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_not_wifi);
        setWindow();
        initViees();

    }

    private void initViees() {
        //初始化view
        tv_title = (TextView) findViewById(R.id.tv_notice);
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv_sure = (TextView) findViewById(R.id.tv_sure);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        if (!TextUtils.isEmpty(title)) tv_title.setText(title);
        if (!TextUtils.isEmpty(content)) tv_content.setText(content);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null != clickListener)
                    clickListener.onClick(v);
            }
        };
        tv_sure.setOnClickListener(onClickListener);
        tv_cancel.setOnClickListener(onClickListener);
    }

    /**
     * 设置dialog窗口宽度
     */
    private void setWindow() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //获取屏幕宽度以设置弹出dialog的宽度
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        lp.width = dm.widthPixels;//宽固定
        getWindow().setAttributes(lp);
    }

}
