package com.yq.library_download;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * Created by yiqile-21 on 2016/7/26.
 */
public class NewVersionDialog extends Dialog {

    private Context context;
    private String content;//更新内容
    private TextView tv_content;//更新内容
    private TextView tv_sure;//確定
    private TextView tv_cancel;//取消
    private View.OnClickListener clickListener;

    public NewVersionDialog(Context context, String content, View.OnClickListener clickListener) {
        super(context, R.style.NobackDialog);
        this.context = context;
        this.content = content;
        this.clickListener = clickListener;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new_version);
        setWindowSize();
        initViews();


    }

    private void initViews() {
        //初始化view
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv_sure = (TextView) findViewById(R.id.tv_sure);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
//        tv_content.setText(content);
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
     * 设置diaolog窗口大小
     */
    protected void setWindowSize() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //获取屏幕宽度以设置弹出dialog的宽度
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        lp.width = dm.widthPixels;//宽固定
        getWindow().setAttributes(lp);
    }

}
