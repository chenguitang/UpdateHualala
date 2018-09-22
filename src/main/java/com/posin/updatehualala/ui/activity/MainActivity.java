package com.posin.updatehualala.ui.activity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.posin.updatehualala.R;
import com.posin.updatehualala.base.BaseActivity;
import com.posin.updatehualala.utils.AppCompanyUtils;
import com.posin.updatehualala.utils.Proc;
import com.posin.updatehualala.utils.StorageUtils;
import com.posin.updatehualala.view.InstallerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final String BASE_UPDATE_FILE = "data/local/tmp/update.ppk";

    /**
     * 文件复制到data/local/tmp成功
     */
    private static final int COPY_SUCCESS = 100;
    /**
     * 文件复制到data/local/tmp失败
     */
    private static final int COPY_FAILURE = 101;
    /**
     * 检查是否需要分区，更新分区更新包
     */
    private static final int CHECK_UPDATE = 102;

    /**
     * 定制客户
     */
    private String systemCompanyName;


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COPY_SUCCESS:
                    dismissLoadingDialog();
                    updatePpk(new File(BASE_UPDATE_FILE));
                    break;
                case COPY_FAILURE:
                    dismissLoadingDialog();
                    Toast.makeText(MainActivity.this, "文件操作出错了，请重新检查更新系统。",
                            Toast.LENGTH_SHORT).show();
                    break;
                case CHECK_UPDATE:
                    if (checkUpdate()) {
                        copyUpdatePackage("update-sdcard-hualala.ppk");
                    }
                    break;
                default:
                    break;

            }
        }
    };


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initData() {

        try {
            systemCompanyName = AppCompanyUtils.getSystemCompanyName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btn_cancel_limit_install})
    public void onClick(View v) {

        if (!TextUtils.isEmpty(systemCompanyName)) {
            if (!systemCompanyName.equals("hualala")) {
                Toast.makeText(this, "此功能只对部分定制客户有限，请确认您的机器.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }


        showLoadingDialog("正在检查是否需要重新分区");

        mHandler.sendEmptyMessageDelayed(CHECK_UPDATE, 1000);
    }

    /**
     * 拷贝更新文件到 data/local/tmp
     */
    private void copyUpdatePackage(final String fileName) {

        new Thread() {
            @Override
            public void run() {
                try {

                    int result_code = Proc.suExecCallback("touch data/local/tmp/update.ppk  \n " +
                            " busybox chmod 777 data/local/tmp/update.ppk", null, 2000);

                    Log.d(TAG, "result_code: " + result_code);
                    if (result_code != 0) {
                        mHandler.obtainMessage(COPY_FAILURE).sendToTarget();
                        return;
                    }

                    byte[] mByte = new byte[1024];
                    int len = 1;
                    InputStream is = getAssets().open(fileName);
                    OutputStream os = new FileOutputStream(BASE_UPDATE_FILE);

                    while ((len = is.read(mByte)) != -1) {
                        os.write(mByte, 0, len);
                    }

                    mHandler.sendEmptyMessageDelayed(COPY_SUCCESS, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(COPY_FAILURE).sendToTarget();
                }
            }
        }.start();

    }

    /**
     * 检查是否需要重新分区
     *
     * @return boolean
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean checkUpdate() {

        String sdTotalSize = StorageUtils.getSDTotalSize(this);
        Log.d(TAG, "SD卡总大小为： " + StorageUtils.getSDTotalSize(this));
        Log.d(TAG, "机身内存总大小为： " + StorageUtils.getRomTotalSize(this));
        double sdcardSize = Double.parseDouble(sdTotalSize.substring(0, sdTotalSize.length() - 3));

        //DATA分区与Sdcard分区大小一致，说明已合并分区，无法重新分区
        if (sdcardSize > 1.5 && sdTotalSize.equals(StorageUtils.getRomTotalSize(this))) {
            Toast.makeText(this, "DATA分区与SDCARD分区已合并，无需重新分区.", Toast.LENGTH_SHORT).show();
            dismissLoadingDialog();
            return false;
        } else if (sdcardSize > 1) { //获取SD卡大小，如果大于1G，足够使用不需要重新分区
            Toast.makeText(this, "SDCARD分区已经是足够大, 无需重新分区.", Toast.LENGTH_SHORT).show();
            dismissLoadingDialog();
            return false;
        } else {
            showLoadingDialog("需要重新分区，正在加载更新文件");
            return true;
        }
    }


    /**
     * 更新PPK
     *
     * @param file
     */
    private void updatePpk(File file) {
        new InstallerDialog(this, file) {
            @Override
            protected void onInstallError() {
                Toast.makeText(MainActivity.this, "安装失败,请重新检查更新系统", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onInstallSuccess() {

            }

            @Override
            protected void onClickOk(boolean updateSuccess) {
                if (updateSuccess) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("更新成功,马上重启！");
                    builder.setPositiveButton("重启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Proc.createSuProcess("reboot");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        };
    }

}

