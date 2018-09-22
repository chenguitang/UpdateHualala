package com.posin.updatehualala.utils;

import android.text.TextUtils;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by Greetty on 2018/1/20.
 * <p>
 * Launcher3 公司定制工具类
 */
public class AppCompanyUtils {

    /**
     * 定制客户为哗啦啦
     */
    public static final String COMPANY_ANMAI_HUALALA = "hualala";

    /**
     * 保存定制客户名称，减少查询系统文件次数
     *
     * 弊端：手动修改配置后，无法实时更新
     */
    private static String mCompanyName = "";


    /**
     * 获取系统配置中的定制客户
     *
     * @return 客户ID
     * @throws Exception 读写文件异常
     */
    public static String getSystemCompanyName() throws Exception {

        if (TextUtils.isEmpty(mCompanyName)) {
            Properties pro = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream("/system/build.prop");
                pro.load(fis);
                mCompanyName = pro.getProperty("ro.cust.name");
                return mCompanyName;
            }finally {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            }
        } else {
            return mCompanyName;
        }
    }

}
