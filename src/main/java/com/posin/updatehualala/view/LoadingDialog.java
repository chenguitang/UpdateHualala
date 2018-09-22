package com.posin.updatehualala.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.TextView;

import com.posin.updatehualala.R;


/**
 * FileName: LoadingDialog
 * Author: Greetty
 * Time: 2018/9/22 13:32
 * Desc: TODO
 */
public class LoadingDialog extends Dialog {

    private String mTitle;
    private TextView tvTitle;

    public LoadingDialog(Context context, String title) {
        super(context, R.style.LoadingDialog);
        this.mTitle = title;
        initView();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                if (LoadingDialog.this.isShowing())
//                    LoadingDialog.this.dismiss();
//                break;
//        }
//        return true;
//    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.dialog_loading_view);
        tvTitle = ((TextView) findViewById(R.id.tv_loading_title));
        if (!TextUtils.isEmpty(mTitle))
            setTitle(mTitle);
        setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = 0.9f;
        getWindow().setAttributes(attributes);
        setCancelable(false);
    }

    /**
     * 修改loading显示的文字
     *
     * @param title String
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }

}
