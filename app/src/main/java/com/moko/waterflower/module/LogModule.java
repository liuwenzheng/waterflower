package com.moko.waterflower.module;

import com.elvishew.xlog.XLog;

/**
 * Log组件
 *
 * @author wenzheng.liu
 */
public class LogModule {
    // tag
    public static String TAG = "waterflower";


    public static void v(String msg) {
        XLog.v(msg);
    }

    public static void i(String msg) {
        XLog.i(msg);
    }

    public static void d(String msg) {
        XLog.d(msg);
    }

    public static void w(String msg) {
        XLog.w(msg);
    }

    public static void e(String msg) {
        XLog.e(msg);
    }
}
