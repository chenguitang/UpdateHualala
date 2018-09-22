package com.posin.updatehualala.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.posin.updatehualala.R;
import com.posin.updatehualala.update.Installer;

import java.io.File;

public abstract class InstallerDialog {
    private final AlertDialog mDlg;
    private final View mView;
    private final LinearLayout mLogView;
    private final Installer mInstaller;
    private final Button mBtnOk;
    private final Button mBtnCancel;
    private boolean updateSuccess = false;

    protected abstract void onInstallError();

    protected abstract void onInstallSuccess();

    protected abstract void onClickOk(boolean updateSuccess);

    public InstallerDialog(Context context, File pkgFile) {

        LayoutInflater inflater = LayoutInflater.from(context);
        mView = inflater.inflate(R.layout.installer_view, null);
        mLogView = (LinearLayout) mView.findViewById(R.id.log_view);

        mInstaller = new Installer(context, pkgFile) {
            @Override
            protected void onStart() {
                mHandler.obtainMessage(MSG_ON_START).sendToTarget();
            }

            @Override
            protected void onStop() {
                mHandler.obtainMessage(MSG_ON_STOP).sendToTarget();
            }

            @Override
            protected void onError(String text) {
                mHandler.obtainMessage(MSG_ON_ERROR, text).sendToTarget();

            }

            @Override
            protected void onAddItem(String text) {
                mHandler.obtainMessage(MSG_ADD_ITEM, text).sendToTarget();
            }

            @Override
            protected void onAddLog(String text) {
                mHandler.obtainMessage(MSG_ADD_LOG, text).sendToTarget();
            }

            @Override
            protected void onSuccess() {
                mHandler.obtainMessage(MSG_SUCCESS).sendToTarget();
            }
        };

        mDlg = (new AlertDialog.Builder(context))
                .setTitle(R.string.installer_view_title)
                .setView(mView)
                .setInverseBackgroundForced(true)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDlg.dismiss();
                        onClickOk(updateSuccess);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mInstaller.stop();
                    }
                })
                .show();

        mBtnOk = mDlg.getButton(DialogInterface.BUTTON_POSITIVE);
        mBtnOk.setEnabled(false);
        mBtnCancel = mDlg.getButton(DialogInterface.BUTTON_NEGATIVE);

        scaleViewSize(mDlg.getWindow(), 0.8f, 0.8f);

        mHandler.obtainMessage(MSG_INSTALL).sendToTarget();
    }

    private static void scaleViewSize(Window win, float scaleX, float scaleY) {
        WindowManager windowManager = win.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = win.getAttributes();
        Point outSize = new Point();

        display.getSize(outSize);
        lp.width = (int) (outSize.x * scaleX);
        lp.height = (int) (outSize.y * scaleY);
        win.setAttributes(lp);

    }

    private static final int MSG_INSTALL = 99;
    private static final int MSG_ADD_ITEM = 100;
    private static final int MSG_ADD_LOG = 101;
    private static final int MSG_ON_START = 102;
    private static final int MSG_ON_STOP = 103;
    private static final int MSG_ON_ERROR = 104;
    private static final int MSG_SUCCESS = 105;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INSTALL:
                    mInstaller.start();
                    break;
                case MSG_ADD_ITEM: {
                    TextView item = new TextView(mView.getContext());
                    item.setText("● " + (String) msg.obj);
                    item.setTextAppearance(mView.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
                    mLogView.addView(item);
                    break;
                }
                case MSG_ADD_LOG: {
                    TextView item = new TextView(mView.getContext());
                    item.setText("    " + (String) msg.obj);
                    item.setTextAppearance(mView.getContext(), android.R.style.TextAppearance_DeviceDefault_Small);
                    mLogView.addView(item);
                    break;
                }
                case MSG_ON_ERROR: {
                    TextView item = new TextView(mView.getContext());
                    item.setText((String) msg.obj);
                    item.setTextAppearance(mView.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
                    item.setTextColor(Color.RED);
                    mLogView.addView(item);
                    onInstallError();
                    break;
                }
                case MSG_SUCCESS:
                    updateSuccess = true;
                    onInstallSuccess();
                    break;
                case MSG_ON_START: {
                    break;
                }
                case MSG_ON_STOP: {
                    mBtnOk.setEnabled(true);
                    mBtnCancel.setEnabled(false);
                    break;
                }
            }

            super.handleMessage(msg);
        }
    };

}
