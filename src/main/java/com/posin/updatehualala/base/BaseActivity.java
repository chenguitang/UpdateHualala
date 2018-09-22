package com.posin.updatehualala.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.posin.updatehualala.view.LoadingDialog;

import butterknife.ButterKnife;

/**
 * FileName: BaseActivity
 * Author: Greetty
 * Time: 2018/9/22 13:31
 * Desc: TODO
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected static Context mContext;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSavedInstanceState(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        setContentView(getLayoutId());
        mContext = this;
        ButterKnife.bind(this);

        initData();
    }

    /**
     * 显示加载进度框
     *
     * @param title String
     */
    public void showLoadingDialog(String title) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, title);
            mLoadingDialog.show();
        } else {
            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.setTitle(title);
                mLoadingDialog.show();
            } else {
                mLoadingDialog.setTitle(title);
            }
        }
    }

    /**
     * 隐藏加载进度框
     */
    public void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                mLoadingDialog = null;
            }
        }
    }

    public abstract int getLayoutId();

    public abstract void initData();


    public void initSavedInstanceState(Bundle savedInstanceState) {
    }

}
